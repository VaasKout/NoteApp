package com.example.noteexample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentSettingsBinding
import com.example.noteexample.viewmodels.SettingsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

//constants for filtering notes
const val ALL = 0
const val TEXT_ONLY = 1
const val PHOTOS_ONLY = 2

@AndroidEntryPoint
class SettingsFragment : BottomSheetDialogFragment() {

    //viewModel
    private val viewModel: SettingsViewModel by viewModels()

    /**
     * This Fragment set parameters for [SettingsViewModel.flagsLiveData]
     * which sort notes in specific way on Main fragment
     *
     * @see com.example.noteexample.database.Flags
     * @see AllNotesFragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /**
         * Binding for SettingsFragment
         * @see R.layout.fragment_settings
         */
        val binding: FragmentSettingsBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)


        /**
         * [getDialog] listener triggers motion layout animation when state changes
         */
        dialog?.setOnShowListener {
            val bottomSheetBehavior = (dialog as BottomSheetDialog).behavior

            val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            binding.motionSettings.transitionToEnd()
                        }
                        else -> {
                            binding.motionSettings.transitionToStart()
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            }
            bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        }

        viewModel.flagsLiveData.observe(viewLifecycleOwner, {
            viewModel.flags = it
            if (it.showDate) {
                binding.switchDate.isChecked = true
            }

            if (it.columns == 2) {
                binding.twoColumns.isChecked = true
            } else {
                binding.oneColumn.isChecked = true
            }

            if (it.ascendingOrder) {
                binding.sortASC.isChecked = true
            } else {
                binding.sortDESC.isChecked = true
            }
            when (it.filter) {
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

        /**
         * Show or hide date
         * @see R.layout.recycler_main_item
         */
        binding.switchDate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flags?.showDate = true
                viewModel.updateFlags()
            } else {
                viewModel.flags?.showDate = false
                viewModel.updateFlags()
            }
        }

        /**
         * Set columns count for recyclerView adapter
         */


        binding.oneColumn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flags?.columns = 1
                viewModel.updateFlags()
            }
        }

        binding.twoColumns.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flags?.columns = 2
                viewModel.updateFlags()
            }
        }

        /**
         *  Sort notes by position
         *  @see com.example.noteexample.database.Header.pos
         */


        binding.sortDESC.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flags?.ascendingOrder = false
                viewModel.updateFlags()
            }
        }

        binding.sortASC.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flags?.ascendingOrder = true
                viewModel.updateFlags()
            }
        }

        /**
         * Filter notes by empty or not empty list of
         * [com.example.noteexample.database.NoteWithImages.images]
         */

        binding.filterAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flags?.filter = ALL
                viewModel.updateFlags()
            }
        }

        binding.filterTextOnly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flags?.filter = TEXT_ONLY
                viewModel.updateFlags()
            }
        }

        binding.filterPhotosOnly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.flags?.filter = PHOTOS_ONLY
                viewModel.updateFlags()
            }
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}