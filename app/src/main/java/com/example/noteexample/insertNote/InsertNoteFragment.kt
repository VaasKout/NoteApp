package com.example.noteexample.insertNote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
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
import kotlinx.coroutines.launch

class InsertNoteFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //binding for EditNoteFragment
        val binding: FragmentInsertNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_insert_note, container, false)

        /**
         * define viewModel for EditNoteFragment
         */
        val viewModel =
            ViewModelProvider(this).get(InsertNoteViewModel::class.java)
        binding.viewModel = viewModel
        val noteAdapter = OneNoteEditAdapter()
        binding.insertRecycler.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
        }

        var id = -1
        viewModel.currentNote.observe(viewLifecycleOwner, {
            if (it != null) {
                id = it.id
            }
        })

        lifecycleScope.launch {
            if (!viewModel.noteInserted) {
                val note = Note()
                viewModel.onInsert(note)
                viewModel.noteInserted = true
            }
            viewModel.getLastNote()
            viewModel.allNoteContent.observe(viewLifecycleOwner, {
                val list = it.filter { list -> list.noteId == id }
                noteAdapter.submitList(list)
            })
        }

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
//                                    GlideApp.with(this)
//                                        .load(camera.currentPhotoPath)
//                                        .into(binding.testImage)
                                }
                                1 -> {
                                    this@InsertNoteFragment.findNavController()
                                        .navigate(
                                            InsertNoteFragmentDirections
                                                .actionEditNoteFragmentToGalleryFragment
                                                    (id)
                                        )
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
//
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