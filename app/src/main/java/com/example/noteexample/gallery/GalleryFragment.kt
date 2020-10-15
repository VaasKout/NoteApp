package com.example.noteexample.gallery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentGalleryBinding
import com.example.noteexample.utils.Camera
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class GalleryFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentGalleryBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)
        val viewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        val camera = Camera(requireActivity())
        val imgList = camera.loadImagesFromStorage()

        val recyclerAdapter = GalleryAdapter(requireActivity())
        recyclerAdapter.submitList(imgList)

        binding.galleryRecyclerView.apply {
            adapter = recyclerAdapter
            setHasFixedSize(true)
        }


        binding.lifecycleOwner = this
        return binding.root
    }
    companion object{
        const val TAG = "ModalBottomSheet"
    }
}