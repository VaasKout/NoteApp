package com.example.noteexample.insertNote

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import com.example.noteexample.utils.OneNoteEditAdapter
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentInsertNoteBinding
import com.example.noteexample.utils.Camera
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InsertNoteFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
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

        /**
         * Initialize viewModel for [InsertNoteViewModel]
         */

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
         * [InsertNoteViewModel.updateCurrentNote] initializes current note, if(it == null)
         * and updates it data
         */

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            if (it != null) {
                Log.e("noteID", "${viewModel.note?.id}")
                val list = it.filter { list -> list.noteId == viewModel.note?.id }
                Log.e("photoList", list.toString())
                noteAdapter.addHeaderAndSubmitList(viewModel.note, list)
            }
        })


        noteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->
            val item = noteAdapter.currentList[holder.adapterPosition].noteContent
            item?.let {
                viewModel.noteContentList.add(it)
                holder.binding.noteEditTextFirst.addTextChangedListener { editable ->
                    it.note = editable.toString()
                    if (it.note.isEmpty() && it.photoPath.isEmpty()) {
                        viewModel.deleteNoteContent(it)
                    }
                }
            }

            holder.binding.deleteCircle.setOnClickListener {
                item?.let { current ->
                    current.photoPath = ""
                    viewModel.updateNoteContent(current)
                    if (current.note.isEmpty()) {
                        viewModel.deleteNoteContent(current)
                    } else {
                        viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                    }
                    noteAdapter.notifyDataSetChanged()
                }
            }
        })


        noteAdapter.noteHolder.observe(viewLifecycleOwner, { holder ->
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
            /**
             * Set text for [R.layout.header_edit] explicitly to prevent to be cleared
             * after [OneNoteEditAdapter.notifyDataSetChanged] method
             */
            holder.binding.titleEdit.setText(viewModel.title)
            holder.binding.firstNoteEdit.setText(viewModel.firstNote)

            holder.binding.titleEdit.addTextChangedListener {
                viewModel.title = it.toString()
            }
            holder.binding.firstNoteEdit.addTextChangedListener {
                viewModel.firstNote = it.toString()
                if (it.toString().isEmpty() && viewModel.secondNoteInit) {
                    holder.binding.firstNoteEdit.setText(viewModel.secondNote)
                    viewModel.firstNote = viewModel.secondNote
                    viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                    viewModel.noteContentToDelete?.let { delete ->
                        viewModel.deleteNoteContent(delete)
                    }
                    viewModel.secondNoteInit = false
                }
            }
        })

        /**
         *  insert data in database and navigate back to NoteFragment
         */
        viewModel.navigateToNoteFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                if (viewModel.noteContentList.isNotEmpty()) {
                    viewModel.updateNoteContentList(viewModel.noteContentList)
                }
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
                    }
                    !viewModel.backPressed -> {
                        viewModel.updateCurrentNote(viewModel.title, viewModel.firstNote)
                        this@InsertNoteFragment.findNavController().popBackStack()
                        viewModel.onDoneNavigating()
                    }
                }
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
         * toolbar clickListener
         */

        binding.toolbarNoteInsert.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.insert_photo -> {
                    viewModel.updateNoteContentList(viewModel.noteContentList)
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

        binding.lifecycleOwner = this
        return binding.root
    }
}