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
import com.example.noteexample.OneNoteEditAdapter
import com.example.noteexample.R
import com.example.noteexample.database.NoteContent
import com.example.noteexample.databinding.FragmentInsertNoteBinding
import com.example.noteexample.utils.Camera
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InsertNoteFragment : Fragment() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    var noteId = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binding for EditNoteFragment
        val binding: FragmentInsertNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_insert_note, container, false)

        /**
         * initialize viewModel for [InsertNoteViewModel]
         */
        val viewModel =
            ViewModelProvider(this).get(InsertNoteViewModel::class.java)
        binding.viewModel = viewModel
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
                    this.findNavController()
                        .navigate(
                            InsertNoteFragmentDirections
                                .actionEditNoteFragmentToGalleryFragment
                                    (noteId)
                        )
                    Log.e("requestId", "$noteId")
                }
                //TODO Make else
            }
        /**
         * [InsertNoteViewModel.getLastNote] initializes current note, if(it == null)
         * and updates it data
         */
        viewModel.currentNote.observe(viewLifecycleOwner, {
            if (it != null) {
                noteId = it.id
                Log.e("title", it.title)
                Log.e("noteID_observe", "$noteId")
            } else {
                viewModel.updateCurrentNote()
            }
        })

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            val list = it.filter { list -> list.noteId == noteId }
            viewModel.noteContentIsEmpty = list.isEmpty()
            Log.e("photoList", list.toString())
            noteAdapter.submitList(list)
        })


        val noteContentList = mutableListOf<NoteContent>()
        noteAdapter.holder.observe(viewLifecycleOwner, {adapter ->
            val item = noteAdapter.currentList[adapter.adapterPosition]
            noteContentList.add(item)
            adapter.binding.noteEditTextFirst.addTextChangedListener {
                item.note = it.toString()
                noteContentList[adapter.adapterPosition] = item
            }
        })

        /**
         *  insert data in database and navigate back to NoteFragment
         */
        viewModel.navigateToNoteFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                if (noteContentList.isNotEmpty()){
                    viewModel.updateNoteContent(noteContentList)
                }
                when {
                    viewModel.noteContentIsEmpty &&
                            binding.titleEditInsert.text.toString().isEmpty() -> {
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
                                viewModel.updateCurrentNote(binding.titleEditInsert.text.toString())
                                this@InsertNoteFragment.findNavController().popBackStack()
                                viewModel.onDoneNavigating()
                            }.show()

                    }
                    !viewModel.backPressed -> {
                        //TODO update note
                        viewModel.updateCurrentNote(binding.titleEditInsert.text.toString())
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
                    val camera = Camera(requireActivity())
                    val items = camera.dialogList
                    MaterialAlertDialogBuilder(requireContext())
                        .setItems(items) { _, index ->
                            when (index) {
                                0 -> {
                                    camera.dispatchTakePictureIntent(binding.saveButton)
                                }
                                1 -> {
                                    if (ContextCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        if (noteId != -1) {
                                            this@InsertNoteFragment.findNavController()
                                                .navigate(
                                                    InsertNoteFragmentDirections
                                                        .actionEditNoteFragmentToGalleryFragment
                                                            (noteId)
                                                )
                                            Log.e("permissionAccessID", "$noteId")
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