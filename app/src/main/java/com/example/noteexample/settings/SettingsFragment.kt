package com.example.noteexample.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentSettingsBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        binding.lifecycleOwner = this
        val viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

//        dialog?.let {
//            val bottomSheetBehavior = (dialog as BottomSheetDialog).behavior
//
//            val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
//                override fun onStateChanged(bottomSheet: View, newState: Int) {
//                    when (newState) {
//                        BottomSheetBehavior.STATE_EXPANDED -> {
//                        }
//                        else -> {
//                        }
//                    }
//                }
//                override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                }
//            }
////            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//            bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
//        }

        viewModel.flags.observe(viewLifecycleOwner, {
            viewModel.flagsObj = it
            if (it.showDate) {
                binding.switchDate.isChecked = true
            }

            if (it.twoColumns){
                binding.twoColumns.isChecked = true
            } else{
                binding.oneColumn.isChecked = true
            }

            if (it.ascendingOrder) {
                binding.sortASC.isChecked = true
            } else {
                binding.sortDESC.isChecked = true
            }
            when {
                it.onlyPhotos -> {
                    binding.filterOnlyPhotos.isChecked = true
                }
                it.onlyNotes -> {
                    binding.filterOnlyNotes.isChecked = true
                }
                else -> {
                    binding.filterAll.isChecked = true
                }
            }
        })

        binding.switchDate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flagsObj?.showDate = true
                viewModel.updateFlags()
            } else {
                viewModel.flagsObj?.showDate = false
                viewModel.updateFlags()
            }
        }

        binding.oneColumn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flagsObj?.twoColumns = false
                viewModel.updateFlags()
            }
        }

        binding.twoColumns.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                viewModel.flagsObj?.twoColumns = true
                viewModel.updateFlags()
            }
        }

        binding.sortDESC.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flagsObj?.ascendingOrder = false
                viewModel.updateFlags()
            }
        }

        binding.sortASC.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flagsObj?.ascendingOrder = true
                viewModel.updateFlags()
            }
        }

        binding.filterAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flagsObj?.onlyNotes = false
                viewModel.flagsObj?.onlyPhotos = false
                viewModel.updateFlags()
            }
        }

        binding.filterOnlyPhotos.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flagsObj?.onlyPhotos = true
                viewModel.flagsObj?.onlyNotes = false
                viewModel.updateFlags()
            }
        }

        binding.filterOnlyNotes.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flagsObj?.onlyNotes = true
                viewModel.flagsObj?.onlyPhotos = false
                viewModel.updateFlags()
            }
        }


        return binding.root
    }
}