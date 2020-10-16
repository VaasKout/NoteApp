package com.example.noteexample.insertNote


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentInsertNoteBinding
import com.example.noteexample.gallery.GalleryFragment


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

        /**
         * toolbar clickListener
         */

        binding.toolbarNoteInsert.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.insert_photo -> {
//                    camera.dispatchTakePictureIntent(binding.saveButton)
//                    Glide.with(this)
//                        .load(camera.currentPhotoPath)
//                        .into(binding.testImage)
//                    if (::intent.isInitialized){
//                        camera.albumImages(intent)
//                    }
                    val modalBottomSheet = GalleryFragment()
                    modalBottomSheet.show(childFragmentManager, "GalleryFragment")

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