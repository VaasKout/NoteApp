package com.example.noteexample.insertNote

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.noteexample.utils.adapter.OneNoteEditAdapter
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentInsertNoteBinding
import com.example.noteexample.utils.Camera
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class InsertNoteFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var startCamera: ActivityResultLauncher<Intent>

    /**
     * Initialize viewModel for [InsertNoteViewModel]
     */

    val viewModel by lazy {
        ViewModelProvider(this).get(InsertNoteViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val camera = Camera(requireActivity())

        //binding for EditNoteFragment
        val binding: FragmentInsertNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_insert_note, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        /**
         * Initialize [OneNoteEditAdapter] for [FragmentInsertNoteBinding.insertRecycler]
         */

        val noteAdapter = OneNoteEditAdapter()
        binding.insertRecycler.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
        }

        /**
         * Initialize two [ActivityResultLauncher] for [Camera] actions
         */

        //Launcher for storage
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    viewModel.note?.let {
                        this.findNavController()
                            .navigate(
                                InsertNoteFragmentDirections
                                    .actionEditNoteFragmentToGalleryFragment
                                        (it.id)
                            )
                    }
                } else {
                    Snackbar.make(
                        binding.saveButton,
                        R.string.camera_request_failed,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        //Launcher for camera itself
        startCamera =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    viewModel.insertCameraPhoto(camera.currentPhotoPath)
                    noteAdapter.notifyDataSetChanged()
                }
            }

        /**
         * toolbar clickListener
         */

        binding.toolbarNoteInsert.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.insert_photo -> {
                    viewModel.updateNoteContentList(viewModel.noteContentList)
                    viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                    val items = camera.dialogList
                    MaterialAlertDialogBuilder(requireContext())
                        .setItems(items) { _, index ->
                            when (index) {
                                0 -> {
                                    startCamera.launch(
                                        camera.dispatchTakePictureIntent(binding.saveButton)
                                    )
                                }
                                1 -> {
                                    if (ContextCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        viewModel.note?.let { note ->
                                            this@InsertNoteFragment.findNavController()
                                                .navigate(
                                                    InsertNoteFragmentDirections
                                                        .actionEditNoteFragmentToGalleryFragment
                                                            (note.id)
                                                )
                                        }
                                    } else {
                                        requestPermissionLauncher.launch(
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        )
                                    }
                                }
                            }
                        }.show()
                    true
                }
                else -> false
            }
        }

        viewModel.allNotes.observe(viewLifecycleOwner, {
            viewModel.size = it.size - 1
        })


        /**
         * [InsertNoteViewModel.updateCurrentNote] initializes current note, if(it == null)
         * and updates it data
         */

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.noteContentList = it.filter { list -> list.noteId == viewModel.note?.id }
                noteAdapter.addHeaderAndSubmitList(viewModel.note, viewModel.noteContentList)
            }
        })

        /**
         * LiveData for holders from [OneNoteEditAdapter]
         */

        noteAdapter.noteHolder.observe(viewLifecycleOwner, { holder ->

            noteAdapter.currentList[holder.adapterPosition].note?.let { _ ->

                holder.binding.titleEdit.addTextChangedListener {
                    viewModel.title = it.toString()
                }
                holder.binding.firstNoteEdit.addTextChangedListener {
                    viewModel.firstNote = it.toString()
                }
            }
        })

        noteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->
            noteAdapter.currentList[holder.adapterPosition].noteContent?.let { current ->
                if (current.hidden) {
                    holder.binding.photo.visibility = View.GONE
                    holder.binding.restoreButton.visibility = View.VISIBLE
                    holder.binding.deleteCircleIcon.visibility = View.GONE
                    holder.binding.deleteCircle.visibility = View.GONE
                } else {
                    holder.binding.photo.visibility = View.VISIBLE
                    holder.binding.restoreButton.visibility = View.GONE
                    holder.binding.deleteCircleIcon.visibility = View.VISIBLE
                    holder.binding.deleteCircle.visibility = View.VISIBLE
                }
            }

            holder.binding.noteEditTextFirst.addTextChangedListener { editable ->
                noteAdapter.currentList[holder.adapterPosition].noteContent?.note =
                    editable.toString()
            }

            holder.binding.deleteCircle.setOnClickListener {
                noteAdapter.currentList[holder.adapterPosition].noteContent?.hidden = true
                viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                viewModel.updateNoteContentList(viewModel.noteContentList)
                noteAdapter.notifyItemChanged(holder.adapterPosition)
            }

            holder.binding.restoreButton.setOnClickListener {
                noteAdapter.currentList[holder.adapterPosition].noteContent?.hidden = false
                viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                viewModel.updateNoteContentList(viewModel.noteContentList)
                noteAdapter.notifyItemChanged(holder.adapterPosition)

            }
        })


        /**
         * Back button clickListener with dialog window, it is called if note wasn't empty,
         * to prevent accidentally remove data
         */

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.backPressed = true
            viewModel.onStartNavigating()
        }

        /**
         *  insert data in database and navigate back to NoteFragment
         */

        viewModel.navigateToNoteFragment.observe(viewLifecycleOwner, {
            if (it == true) {

                if (viewModel.noteContentList.any { item -> !item.hidden } ||
                    viewModel.noteContentList.any { item -> item.note.isNotEmpty() }) {
                    viewModel.allHidden = false
                }

                viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                viewModel.updateNoteContentList(viewModel.noteContentList)

                when {
                    (viewModel.noteContentList.isEmpty() ||
                            viewModel.allHidden) &&
                            viewModel.title.isEmpty() &&
                            viewModel.firstNote.isEmpty()
                    -> {
                        this@InsertNoteFragment.findNavController().popBackStack()
                        viewModel.deleteUnused()
                        viewModel.onDoneNavigating()
                    }
                    viewModel.backPressed -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage("Сохранить изменения?")
                            .setNegativeButton("Нет") { _, _ ->
                                viewModel.deleteUnused()
                                this@InsertNoteFragment.findNavController().popBackStack()
                                viewModel.onDoneNavigating()
                            }
                            .setPositiveButton("Да") { _, _ ->
                                this@InsertNoteFragment.findNavController().popBackStack()
                                viewModel.onDoneNavigating()
                            }.show()
                        viewModel.onDoneNavigating()
                    }
                    !viewModel.backPressed -> {
                        this@InsertNoteFragment.findNavController().popBackStack()
                        viewModel.onDoneNavigating()
                    }
                }
            }
        })

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateNoteContentList(viewModel.noteContentList)
        viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
    }
}


//                if (!viewModel.secondNoteInit){
//                    noteAdapter.currentList.forEach {dataItem ->
//                        dataItem.noteContent?.let { current ->
//                            if (current.note.isNotEmpty() &&
//                                current.photoPath.isEmpty()) {
//                                viewModel.secondNote = current.note
//                                viewModel.noteContentToDelete = current
//                                viewModel.secondNoteInit = true
//                                return@forEach
//                            }
//                        }
//                    }
//                }
//
//                checkText(holder.binding.firstNoteEdit)


//        fun checkText(editText: EditText) {
//            if (editText.text.isEmpty() &&
//                viewModel.secondNoteInit &&
//                viewModel.noteContentList.isNotEmpty()
//            ) {
//                editText.setText(viewModel.secondNote)
//                viewModel.firstNote = viewModel.secondNote
//                viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
//                viewModel.updateNoteContentList(viewModel.noteContentList)
//                viewModel.noteContentToDelete?.let { delete ->
//                    viewModel.deleteNoteContent(delete)
//                }
//                viewModel.secondNoteInit = false
//            }
//        }

//            checkText(holder.binding.firstNoteEdit)
