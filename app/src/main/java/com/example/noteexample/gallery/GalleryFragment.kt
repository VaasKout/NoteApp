package com.example.noteexample.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentGalleryBinding
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

        /**
         * RecyclerView options
         */
        binding.galleryRecyclerView.apply {
            adapter = galleryAdapter
            setHasFixedSize(true)
        }
        viewModel.getData(requireActivity())
        viewModel.galleryData.observe(viewLifecycleOwner, {
            galleryAdapter.submitList(it)
        })

        galleryAdapter.holder.observe(viewLifecycleOwner, { adapter ->
            val item = galleryAdapter.currentList[adapter.adapterPosition]

            adapter.binding.galleryCard.setOnClickListener {
                item.isChecked = !item.isChecked
                viewModel.galleryData.value?.let {
                    binding.numberOfSelected.text =
                        it.filter { list -> list.isChecked }.size.toString()

                    if (it.any { list -> list.isChecked }){
                        binding.selectPanel.visibility = View.VISIBLE
                        binding.galleryTitle.visibility = View.GONE
                    } else {
                        binding.selectPanel.visibility = View.GONE
                        binding.galleryTitle.visibility = View.VISIBLE
                    }
                }
                galleryAdapter.notifyDataSetChanged()
            }
        })

        binding.deleteSelectedPhotos.setOnClickListener {
            viewModel.clearSelected()
            galleryAdapter.notifyDataSetChanged()
            binding.selectPanel.visibility = View.GONE
            binding.galleryTitle.visibility = View.VISIBLE
        }

        binding.acceptSelectedPhotos.setOnClickListener {
            viewModel.insertImages(args.noteId)
            this.findNavController().popBackStack()
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}