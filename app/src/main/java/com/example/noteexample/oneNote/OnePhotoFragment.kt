package com.example.noteexample.oneNote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentOnePhotoBinding
import kotlinx.coroutines.launch

class OnePhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args by navArgs<OnePhotoFragmentArgs>()
        val binding: FragmentOnePhotoBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_one_photo, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(this.activity).application
        val viewModelFactory = OneNoteViewModelFactory(application, args.noteID, args.noteContentID)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(OneNoteViewModel::class.java)
        lifecycleScope.launch {
            viewModel.getNote()
            binding.note = viewModel.currentNote
            binding.data = viewModel.currentNoteContent
        }

        /**
         * Menu onClickListener
         */

        binding.toolbarOnePhoto.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }

        binding.toolbarOnePhoto.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_item -> {
                    this.findNavController()
                        .navigate(
                            OnePhotoFragmentDirections
                                .actionOnePhotoFragmentToEditNoteFragment(args.noteID)
                        )
                    true
                }
                else -> false
            }
        }

        binding.imgOnePhoto.setOnClickListener {
            if (!viewModel.imgClicked) {
                viewModel.imgClicked = true
                binding.motionOnePhoto.transitionToEnd()
            } else {
                viewModel.imgClicked = false
                binding.motionOnePhoto.transitionToStart()
            }
        }

        return binding.root
    }
}


//            viewModel.currentNoteContent?.let {
//                if (it.photoPath.isEmpty() && it.note.isNotEmpty()) {
//                    val constraintSet = ConstraintSet()
//                    constraintSet.clone(binding.onePhotoConstraint)
//                    constraintSet.clear(R.id.note_view_one_photo, ConstraintSet.BOTTOM)
//                    constraintSet.connect(
//                        R.id.note_view_one_photo,
//                        ConstraintSet.TOP,
//                        R.id.first_note_view_one_photo,
//                        ConstraintSet.BOTTOM,
//                        0
//                    )
//                    constraintSet.applyTo(binding.onePhotoConstraint)
//                }
//            }