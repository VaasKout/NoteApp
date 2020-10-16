package com.example.noteexample.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentGalleryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class GalleryFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentGalleryBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)
        val viewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        binding.galleryRecyclerView.adapter = GalleryAdapter(requireActivity())
        viewModel.getData(requireActivity())

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
}