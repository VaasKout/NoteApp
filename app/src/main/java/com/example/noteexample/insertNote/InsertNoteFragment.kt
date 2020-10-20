package com.example.noteexample.insertNote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.noteexample.OneNoteEditAdapter
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.databinding.FragmentInsertNoteBinding
import com.example.noteexample.utils.Camera
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InsertNoteFragment : Fragment() {

    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
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
                        this@InsertNoteFragment.findNavController()
                            .navigate(
                                InsertNoteFragmentDirections
                                    .actionEditNoteFragmentToGalleryFragment
                                        (noteId)
                            )
                        Log.e("requestId", "$noteId")
                    }
                }
            if (!viewModel.noteInserted) {
                val note = Note()
                viewModel.onInsert(note)
                viewModel.noteInserted = true
            }
            viewModel.getLastNote()
            viewModel.currentNote.observe(viewLifecycleOwner, {
                if (it != null) {
                    noteId = it.id
                    Log.e("currentNoteId", "$noteId")
                    Log.e("noteId", "$noteId")
                } else {
                    viewModel.getLastNote()
                }
            })

            viewModel.allNoteContent.observe(viewLifecycleOwner, {
                val list = it.filter { list -> list.noteId == noteId }
                Log.e("photoList", list.toString())
                noteAdapter.submitList(list)
            })


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.deleteUnused()
            this@InsertNoteFragment.findNavController().popBackStack()
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
                                            Log.e("permittionAccessID", "$noteId")
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
         *  insert data in database and navigate back to NoteFragment
         */
//        viewModel.navigateToNoteFragment.observe(viewLifecycleOwner, {
//            val title = binding.titleEditText.text.toString()
//            val noteText = binding.noteEditText.text.toString()
//            if (it == true){
//                if (title.isNotEmpty() || noteText.isNotEmpty()){
//                val note = Note(title = title, note = noteText)
//                viewModel.onInsert(note)
//                }
//                this.findNavController()
//                    .navigate(InsertNoteFragmentDirections
//                        .actionEditNoteFragmentToNoteFragment())
//                viewModel.onDoneNavigating()
//            }
//        })

        binding.lifecycleOwner = this
        return binding.root
    }
}