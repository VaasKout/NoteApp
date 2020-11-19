package com.example.noteexample.gallery

import android.os.Bundle
import android.util.TypedValue
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
//                    when(newState){
//                        BottomSheetBehavior.STATE_EXPANDED ->{
//                            binding.lineGallery.visibility = View.GONE
//                            binding.viewGallery.visibility = View.GONE
//                            viewModel.showExpandPanel = true
//                            if (!viewModel.actionModeStarted){
//                                binding.galleryExpandPanel.visibility = View.VISIBLE
//                            }
//                        }
//                        else ->{
//                            binding.lineGallery.visibility = View.VISIBLE
//                            binding.viewGallery.visibility = View.VISIBLE
//                            binding.galleryExpandPanel.visibility = View.GONE
//                            viewModel.showExpandPanel = false
//                        }
//                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    when (slideOffset) {
                        0.1f -> {
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                        1f -> {
                            binding.lineGallery.visibility = View.GONE
//                            binding.viewGallery.visibility = View.GONE
                            viewModel.showExpandPanel = true
                            if (!viewModel.actionModeStarted) {
                                binding.galleryExpandPanel.visibility = View.VISIBLE
                            }
                        }
                        else -> {
                            if (!viewModel.actionModeStarted) {
                                binding.lineGallery.visibility = View.VISIBLE
//                                binding.viewGallery.visibility = View.VISIBLE
                            }
                            binding.galleryExpandPanel.visibility = View.GONE
                            viewModel.showExpandPanel = false
                        }
                    }
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
            binding.numberOfSelected.text =
                viewModel.galleryList.filter { list -> list.isChecked }.size.toString()
        }

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            viewModel.currentNoteContentList = it.filter { list -> list.noteId == args.noteId }
        })

        viewModel.actionMode.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.actionModeStarted = true
                binding.selectPanel.visibility = View.VISIBLE
                binding.lineGallery.visibility = View.GONE
//                binding.viewGallery.visibility = View.GONE
                binding.galleryExpandPanel.visibility = View.GONE
            } else {
                viewModel.actionModeStarted = false
                binding.selectPanel.visibility = View.GONE
                if (viewModel.showExpandPanel) {
                    binding.galleryExpandPanel.visibility = View.VISIBLE
//                    binding.viewGallery.visibility = View.GONE
                } else {
                    binding.lineGallery.visibility = View.VISIBLE
//                    binding.viewGallery.visibility = View.VISIBLE
                }
            }
        })

        galleryAdapter.holder.observe(viewLifecycleOwner, { holder ->
            val card = holder.binding.galleryCard

            card.setOnClickListener {
                card.isChecked = !card.isChecked
                viewModel.galleryList[holder.adapterPosition].isChecked =
                    card.isChecked

                if (!viewModel.actionModeStarted) {
                    viewModel.onStartActionMode()
                } else if (viewModel.actionModeStarted &&
                    galleryAdapter.currentList.none { list -> list.isChecked }
                ) {
                    viewModel.onDoneActionMode()
                }
                binding.numberOfSelected.text =
                    viewModel.galleryList.filter { list -> list.isChecked }.size.toString()
            }
        })

        binding.deleteSelectedPhotos.setOnClickListener {
            viewModel.clearSelected()
            galleryAdapter.notifyDataSetChanged()
            viewModel.onDoneActionMode()
        }

        binding.acceptSelectedPhotos.setOnClickListener {
            lifecycleScope.launch {
                viewModel.insertImages(args.noteId)
                this@GalleryFragment.findNavController().popBackStack()
                viewModel.onDoneActionMode()
            }
        }

        binding.galleryBackButton.setOnClickListener {
            this.findNavController().popBackStack()
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}