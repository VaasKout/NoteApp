package com.example.noteexample.ui

import android.app.Application
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
import com.example.noteexample.adapters.GalleryAdapter
import com.example.noteexample.viewmodels.GalleryViewModel
import com.example.noteexample.utils.Camera
import com.example.noteexample.viewmodels.NoteViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class GalleryFragment : BottomSheetDialogFragment() {


    @Inject lateinit var camera: Camera
    private val args by navArgs<GalleryFragmentArgs>()
    private val viewModel by lazy {
        val application: Application = requireNotNull(this.activity).application
        val galleryViewModelFactory = NoteViewModelFactory(args.noteID, application)
        ViewModelProvider(this, galleryViewModelFactory).get(GalleryViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /**
         * binding for GalleryFragment
         * @see R.layout.fragment_gallery
         */
        val binding: FragmentGalleryBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)

        /**
         * [getDialog] listener triggers motion layout animation when state changes
         */
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
                }
            }
            bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        }

        /**
         * RecyclerView options
         */

        val galleryAdapter = GalleryAdapter()

        viewModel.getData(camera)
        binding.galleryRecyclerView.apply {
            galleryAdapter.submitList(viewModel.galleryList)
            adapter = galleryAdapter
            setHasFixedSize(true)
        }

        /**
         * [GalleryViewModel.actionMode] checks photos to insert
         */
        if (viewModel.galleryList.any { list -> list.isChecked }) {
            viewModel.onStartActionMode()
            binding.galleryMenuTitle.text =
                viewModel.galleryList.filter { list -> list.isChecked }.size.toString()
        }

        viewModel.actionMode.observe(viewLifecycleOwner, {
            if (it == true) {
                viewModel.actionModeStarted = true
                binding.acceptSelectedPhotos.visibility = View.VISIBLE
                binding.galleryMotion.transitionToEnd()
            } else {
                viewModel.actionModeStarted = false
                binding.acceptSelectedPhotos.visibility = View.GONE
                binding.galleryMenuTitle.text = resources.getText(R.string.gallery)
                if (!viewModel.expandedState) {
                    binding.galleryMotion.transitionToStart()
                }
            }
        })


        /**
         * [GalleryAdapter.holder] LiveData
         * onClickListener starts or ends actionMode
         */
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

        /**
         * NavIcon clickListener
         */
        binding.galleryBackButton.setOnClickListener {
            if (viewModel.actionModeStarted) {
                viewModel.clearSelected()
                galleryAdapter.notifyDataSetChanged()
                viewModel.onDoneActionMode()
            } else {
                this.findNavController().popBackStack()
            }
        }

        /**
         * Accept item clickListener
         */
        binding.acceptSelectedPhotos.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Default) {
                viewModel.insertImages()
                withContext(Dispatchers.Main) {
                    this@GalleryFragment.findNavController().popBackStack()
                    viewModel.onDoneActionMode()
                }
            }
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}