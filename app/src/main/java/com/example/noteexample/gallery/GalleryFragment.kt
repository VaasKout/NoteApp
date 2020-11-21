package com.example.noteexample.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentGalleryBinding
import com.example.noteexample.utils.Camera
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_gallery.view.*
import kotlinx.coroutines.launch


class GalleryFragment : BottomSheetDialogFragment() {

    private val args by navArgs<GalleryFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentGalleryBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)
        val viewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        val galleryAdapter = GalleryAdapter()
        val camera = Camera(requireActivity())

        dialog?.setOnShowListener {
            val bottomSheetBehavior = (dialog as BottomSheetDialog).behavior

            val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            binding.galleryMotion.transitionToEnd()
                            viewModel.expandedState = true
                        }
                        else -> {
                            if (!viewModel.actionModeStarted) {
                                binding.galleryMotion.transitionToStart()
                            }
                            viewModel.expandedState = false
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                    when (slideOffset) {
//                        1f -> {
//                            binding.lineGallery.visibility = View.GONE
//                            binding.galleryMenuTitle.visibility = View.VISIBLE
//                            binding.galleryBackButton.visibility = View.VISIBLE
//                        }
//                        else -> {
//                            binding.lineGallery.visibility = View.VISIBLE
//                            binding.galleryMenuTitle.visibility = View.GONE
//                            binding.galleryBackButton.visibility = View.GONE
//                        }
//                    }
                }
            }
            bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        }

        if (!viewModel.galleryListInit) {
            viewModel.getData(camera)
            viewModel.galleryListInit = true
        }

        /**
         * RecyclerView options
         */

        binding.galleryRecyclerView.apply {
            galleryAdapter.submitList(viewModel.galleryList)
            adapter = galleryAdapter
            setHasFixedSize(true)
        }

        if (viewModel.galleryList.any { list -> list.isChecked }) {
            viewModel.onStartActionMode()
            binding.galleryMenuTitle.text =
                viewModel.galleryList.filter { list -> list.isChecked }.size.toString()
        }

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            viewModel.currentNoteContentList = it.filter { list -> list.noteId == args.noteId }
        })

        viewModel.actionMode.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.actionModeStarted = true
                binding.acceptSelectedPhotos.visibility = View.VISIBLE
                binding.galleryMotion.transitionToEnd()
            } else {
                viewModel.actionModeStarted = false
                binding.acceptSelectedPhotos.visibility = View.GONE
                binding.galleryMenuTitle.text = resources.getText(R.string.gallery)
                if (!viewModel.expandedState){
                    binding.galleryMotion.transitionToStart()
                }
            }
        })

        galleryAdapter.holder.observe(viewLifecycleOwner, { holder ->
            val card = holder.binding.galleryCard

            card.setOnClickListener {
                card.isChecked = !card.isChecked
                viewModel.galleryList[holder.adapterPosition].isChecked =
                    card.isChecked
                binding.galleryMenuTitle.text =
                    viewModel.galleryList.filter { list -> list.isChecked }.size.toString()

                if (!viewModel.actionModeStarted) {
                    viewModel.onStartActionMode()
                } else if (viewModel.actionModeStarted &&
                    galleryAdapter.currentList.none { list -> list.isChecked }
                ) {
                    viewModel.onDoneActionMode()
                }
            }
        })

        binding.galleryBackButton.setOnClickListener {
            if (viewModel.actionModeStarted) {
                viewModel.clearSelected()
                galleryAdapter.notifyDataSetChanged()
                viewModel.onDoneActionMode()
            } else {
                this.findNavController().popBackStack()
            }
        }

        binding.acceptSelectedPhotos.setOnClickListener {
            lifecycleScope.launch {
                viewModel.insertImages(args.noteId)
                this@GalleryFragment.findNavController().popBackStack()
                viewModel.onDoneActionMode()
            }
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}