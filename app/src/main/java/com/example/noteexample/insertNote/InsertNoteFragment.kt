package com.example.noteexample.insertNote

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.noteexample.utils.OneNoteEditAdapter
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentInsertNoteBinding
import com.example.noteexample.utils.Camera
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InsertNoteFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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

        //binding for EditNoteFragment
        val binding: FragmentInsertNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_insert_note, container, false)
        binding.viewModel = viewModel


        /**
         * Initialize [OneNoteEditAdapter] for [FragmentInsertNoteBinding.insertRecycler]
         */

        val noteAdapter = OneNoteEditAdapter()
        binding.insertRecycler.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
        }

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
                    Log.e("requestId", "${viewModel.note?.id}")
                }
                //TODO Make else
            }

        /**
         * toolbar clickListener
         */

        binding.toolbarNoteInsert.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.insert_photo -> {
                    viewModel.updateNoteContentList(viewModel.noteContentList)
                    viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                    val camera = Camera(requireActivity())
                    val items = camera.dialogList
                    MaterialAlertDialogBuilder(requireContext())
                        .setItems(items) { _, index ->
                            when (index) {
                                0 -> {
                                    camera.dispatchTakePictureIntent(binding.saveButton)
                                    viewModel.insertPhoto(camera.currentPhotoPath)
                                    Log.e("currentPhotoPath", camera.currentPhotoPath)
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

        /**
         * [InsertNoteViewModel.updateCurrentNote] initializes current note, if(it == null)
         * and updates it data
         */

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            if (it != null) {
                val list = it.filter { list -> list.noteId == viewModel.note?.id }
                viewModel.noteContentList = list
                Log.e("noteID", "${viewModel.note?.id}")
                Log.e("photoList", list.toString())
                noteAdapter.addHeaderAndSubmitList(viewModel.note, list)
            }
        })

        noteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->
            val item = noteAdapter.currentList[holder.adapterPosition].noteContent
            item?.let { current ->
                holder.binding.noteEditTextFirst.addTextChangedListener { editable ->
                    current.note = editable.toString()
                    if (current.note.isEmpty() && current.photoPath.isEmpty()) {
                        Log.e("photoListSize", "${viewModel.noteContentList.size}")
                        viewModel.deleteNoteContent(current)
                    }
                }
                holder.binding.deleteCircle.setOnClickListener {
                    current.photoPath = ""

                    viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                    viewModel.updateNoteContentList(viewModel.noteContentList)

                    holder.binding.noteEditTextFirst.setText(current.note)
                    noteAdapter.notifyDataSetChanged()
                }
            }
        })

        /**
         * LiveData for holders
         */

        fun checkText(editText: EditText){
            if (editText.text.isEmpty() &&
                viewModel.secondNoteInit &&
                viewModel.noteContentList.isNotEmpty()){
                editText.setText(viewModel.secondNote)
                viewModel.firstNote = viewModel.secondNote
                viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                viewModel.noteContentToDelete?.let { delete ->
                    viewModel.deleteNoteContent(delete)
                }
                viewModel.secondNoteInit = false
            }
        }

        noteAdapter.noteHolder.observe(viewLifecycleOwner, { holder ->

            /**
             * Set text for [R.layout.header_edit] explicitly to prevent to be cleared
             * after [OneNoteEditAdapter.notifyDataSetChanged] method
             */
            holder.binding.titleEdit.setText(viewModel.title)
            holder.binding.firstNoteEdit.setText(viewModel.firstNote)
            viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)

            noteAdapter.currentList.forEach {
                it.noteContent?.let { current ->
                    if (current.note.isNotEmpty() && current.photoPath.isEmpty()) {
                        viewModel.secondNote = current.note
                        viewModel.noteContentToDelete = current
                        viewModel.secondNoteInit = true
                        return@forEach
                    }
                }
            }

            holder.binding.titleEdit.addTextChangedListener {
                viewModel.title = it.toString()
            }
            holder.binding.firstNoteEdit.addTextChangedListener {
                viewModel.firstNote = it.toString()
                checkText(holder.binding.firstNoteEdit)
            }
            checkText(holder.binding.firstNoteEdit)
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
                    viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                    viewModel.updateNoteContentList(viewModel.noteContentList)
                Log.e("photoListSize", "${viewModel.noteContentList.size}")
                when {
                    viewModel.noteContentList.isEmpty() &&
                            viewModel.title.isEmpty() &&
                            viewModel.firstNote.isEmpty() -> {
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
                                viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                                this@InsertNoteFragment.findNavController().popBackStack()
                                viewModel.onDoneNavigating()
                            }.show()
                        viewModel.onDoneNavigating()
                    }
                    !viewModel.backPressed -> {
                        viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                        this@InsertNoteFragment.findNavController().popBackStack()
                        viewModel.onDoneNavigating()
                    }
                }
            }
        })

        binding.lifecycleOwner = this
        return binding.root
    }
}