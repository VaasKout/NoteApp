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

const val ALL = 0
const val TEXT_ONLY = 1
const val PHOTOS_ONLY = 2


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

            if (it.columns == 2){
                binding.twoColumns.isChecked = true
            } else {
                binding.oneColumn.isChecked = true
            }

            if (it.ascendingOrder) {
                binding.sortASC.isChecked = true
            } else {
                binding.sortDESC.isChecked = true
            }
            when(it.filter) {
                ALL -> {
                    binding.filterAll.isChecked = true
                }
                TEXT_ONLY -> {
                    binding.filterTextOnly.isChecked = true
                }
                PHOTOS_ONLY -> {
                    binding.filterPhotosOnly.isChecked = true
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
                viewModel.flagsObj?.columns = 1
                viewModel.updateFlags()
            }
        }

        binding.twoColumns.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                viewModel.flagsObj?.columns = 2
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
                viewModel.flagsObj?.filter = ALL
                viewModel.updateFlags()
            }
        }

        binding.filterTextOnly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flagsObj?.filter = TEXT_ONLY
                viewModel.updateFlags()
            }
        }

        binding.filterPhotosOnly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flagsObj?.filter = PHOTOS_ONLY
                viewModel.updateFlags()
            }
        }

        return binding.root
    }
}