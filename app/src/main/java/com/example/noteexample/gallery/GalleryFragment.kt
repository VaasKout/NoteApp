package com.example.noteexample.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentGalleryBinding
import com.example.noteexample.utils.Camera
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class GalleryFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val args by navArgs<GalleryFragmentArgs>()

        val binding: FragmentGalleryBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)
        val viewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        val galleryAdapter = GalleryAdapter()
        val camera = Camera(requireActivity())

        if (!viewModel.galleryListInit){
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

        viewModel.actionMode.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.actionModeStarted = true
                binding.selectPanel.visibility = View.VISIBLE
                binding.galleryTitle.visibility = View.GONE
            } else {
                viewModel.actionModeStarted = false
                binding.selectPanel.visibility = View.GONE
                binding.galleryTitle.visibility = View.VISIBLE
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
            viewModel.insertImages(args.noteId)
            Log.e("argsID", "${args.noteId}")
            this.findNavController().popBackStack()
            viewModel.onDoneActionMode()
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}