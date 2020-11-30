package com.example.noteexample.oneNote

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentOnePhotoBinding
import com.example.noteexample.utils.OnSwipeTouchListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OnePhotoFragment : Fragment() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args by navArgs<OnePhotoFragmentArgs>()
        val binding: FragmentOnePhotoBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_one_photo, container, false)
        binding.lifecycleOwner = this

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


        // finally change the color
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireActivity(), R.color.grey_material)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = OneNoteViewModelFactory(application, args.noteID)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(OneNoteViewModel::class.java)

        viewModel.currentNoteLiveData.observe(viewLifecycleOwner, {
            binding.note = it.note
            if (it.note.title.isNotEmpty()) {
                binding.titleViewOnePhoto.visibility = View.VISIBLE
            }
            if (it.note.text.isNotEmpty()) {
                binding.firstNoteViewOnePhoto.visibility = View.VISIBLE
            }
            if (it.images.isNotEmpty()) {
                val img = it.images.filter { item -> item.imgID == args.imgID }[0]
                binding.data = img
                if (img.signature.isNotEmpty()) {
                    binding.noteViewOnePhoto.visibility = View.VISIBLE
                }
            }
        })

        /**
         * Menu onClickListener
         */

        binding.toolbarOnePhoto.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }

        binding.toolbarOnePhoto.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_item -> {
                    this.findNavController()
                        .navigate(
                            OnePhotoFragmentDirections
                                .actionOnePhotoFragmentToEditNoteFragment(args.noteID)
                        )
                    true
                }
                else -> false
            }
        }

        binding.imgOnePhoto.setOnClickListener {
            binding.motionOnePhoto.setTransition(R.id.start, R.id.endHide)

        }


        binding.imgOnePhoto.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeTop() {
                if (!binding.imgOnePhoto.isZoomed) {
                        binding.titleViewOnePhoto.visibility = View.INVISIBLE
                        binding.firstNoteViewOnePhoto.visibility = View.INVISIBLE
                        binding.noteViewOnePhoto.visibility = View.INVISIBLE
                        binding.toolbarOnePhoto.visibility = View.INVISIBLE
                        binding.motionOnePhoto.setTransition(R.id.start, R.id.endUp)
                        binding.motionOnePhoto.transitionToEnd()
                    lifecycleScope.launch {
                        delay(150)
                        this@OnePhotoFragment.findNavController().popBackStack()
                    }
                }
            }

            override fun onSwipeBottom() {
                if (!binding.imgOnePhoto.isZoomed) {
                        binding.titleViewOnePhoto.visibility = View.INVISIBLE
                        binding.firstNoteViewOnePhoto.visibility = View.INVISIBLE
                        binding.noteViewOnePhoto.visibility = View.INVISIBLE
                        binding.toolbarOnePhoto.visibility = View.INVISIBLE
                        binding.motionOnePhoto.setTransition(R.id.start, R.id.endDown)
                        binding.motionOnePhoto.transitionToEnd()
                    lifecycleScope.launch {
                        delay(150)
                        this@OnePhotoFragment.findNavController().popBackStack()
                    }
                }
            }
        })

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()

        requireActivity().window
            .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireActivity(), R.color.primaryDarkColor)
    }
}







//            viewModel.currentNoteContent?.let {
//                if (it.photoPath.isEmpty() && it.note.isNotEmpty()) {
//                    val constraintSet = ConstraintSet()
//                    constraintSet.clone(binding.onePhotoConstraint)
//                    constraintSet.clear(R.id.note_view_one_photo, ConstraintSet.BOTTOM)
//                    constraintSet.connect(
//                        R.id.note_view_one_photo,
//                        ConstraintSet.TOP,
//                        R.id.first_note_view_one_photo,
//                        ConstraintSet.BOTTOM,
//                        0
//                    )
//                    constraintSet.applyTo(binding.onePhotoConstraint)
//                }
//            }