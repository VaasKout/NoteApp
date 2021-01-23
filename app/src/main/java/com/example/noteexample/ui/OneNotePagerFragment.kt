package com.example.noteexample.ui

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.noteexample.R
import com.example.noteexample.adapters.FirstNoteAdapter
import com.example.noteexample.adapters.ViewPagerAdapter
import com.example.noteexample.database.FirstNote
import com.example.noteexample.databinding.FragmentOneNotePagerBinding
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.CustomTouchListener
import com.example.noteexample.viewmodels.OneNoteViewModel
import com.example.noteexample.viewmodels.NoteViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO adapter for only first note in OneNoteFragment
//TODO make ViewNoteAdapterWork
//TODO check order of items in AllNotesFragment in card

@AndroidEntryPoint
class OneNotePagerFragment : Fragment() {

    @Inject
    lateinit var repository: NoteRepository
    lateinit var binding: FragmentOneNotePagerBinding
    private val args by navArgs<OneNotePagerFragmentArgs>()
    private val pagerAdapter = ViewPagerAdapter()
    private val firstNoteAdapter = FirstNoteAdapter(true)


    //viewModel
    private val viewModel: OneNoteViewModel by viewModels {
        NoteViewModelFactory(args.noteID, repository)
    }


    init {
        lifecycleScope.launchWhenStarted {
            //flags for changes of status bar
            requireActivity().window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireActivity(), R.color.grey_material)

        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_one_note_pager, container, false)


        /**
         * Visibility of text fields depends on [OneNoteViewModel.currentNoteLiveData] value
         */
        viewModel.currentNoteLiveData.observe(viewLifecycleOwner, {
            binding.header = it.header
            if (it.header.title.isNotEmpty()) {
                binding.titleViewOnePhoto.visibility = View.VISIBLE
            }
            binding.recyclerInViewPager.adapter = firstNoteAdapter
            binding.photoPager.adapter = pagerAdapter
            lifecycleScope.launch {
                if (pagerAdapter.currentList.isEmpty()) {
                    pagerAdapter.submitList(it.images)
                    binding.photoPager.setCurrentItem(args.pos, false)
                }
                if (firstNoteAdapter.currentList.isEmpty()) {
                    firstNoteAdapter.submitList(it.notes)
                }

            }
        })

        /**
         * Menu onClickListener
         */

        binding.toolbarOneNotePager.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }

        binding.toolbarOneNotePager.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_item -> {
                    this.findNavController()
                        .navigate(
                            OneNotePagerFragmentDirections
                                .actionOnePhotoFragmentToEditNoteFragment(args.noteID)
                        )
                    true
                }
                else -> false
            }
        }

        binding.photoPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (pagerAdapter.currentList[position].signature.isNotEmpty()) {
                    binding.noteViewOnePhoto.visibility = View.VISIBLE
                    binding.noteViewOnePhoto.text =
                        pagerAdapter.currentList[position].signature
                } else {
                    binding.noteViewOnePhoto.visibility = View.GONE
                }
                binding.toolbarOneNotePager.title =
                    "${position + 1}/${pagerAdapter.currentList.size}"
            }
        })


        /**
         * Set animation to hide text and binding.toolbarOnePhoto
         * @see R.xml.fragment_one_photo_xml_one_photo_constraint_scene
         */

//        binding.photoPager.isUserInputEnabled = false
        firstNoteAdapter.firstNoteTodoHolder.observe(viewLifecycleOwner, { holder ->
            firstNoteAdapter.currentList[holder.absoluteAdapterPosition]?.also {

                holder.binding.checkboxView.setOnCheckedChangeListener { _, isChecked ->
                    holder.binding.checkboxView.isChecked = isChecked
                    it.isChecked = isChecked
                    viewModel.updateFirstNote(it)
//                    crossText(it, holder.binding.checkboxView)
                }
            }
        })

        pagerAdapter.holder.observe(viewLifecycleOwner, { holder ->

            holder.binding.imgOnePhoto.setOnClickListener {
                //catches current state of zoom
                binding.photoPager.isUserInputEnabled =
                    !holder.binding.imgOnePhoto.isZoomed
                if (!viewModel.animationOnEnd) {
                    binding.motionOnePhoto.transitionToEnd()
                    viewModel.animationOnEnd = true
                } else {
                    binding.motionOnePhoto.transitionToStart()
                    viewModel.animationOnEnd = false
                }
            }

            holder.binding.imgOnePhoto
                .setOnTouchListener(object : CustomTouchListener(requireContext()) {

                    override fun onSwipeTop() {

                        //check if image zoomed
                        if (!holder.binding.imgOnePhoto.isZoomed) {
                            binding.titleViewOnePhoto.visibility = View.INVISIBLE
                            binding.recyclerInViewPager.visibility = View.INVISIBLE
                            binding.noteViewOnePhoto.visibility = View.INVISIBLE
                            binding.toolbarOneNotePager.visibility = View.INVISIBLE
                            holder.binding.motionPagerItem
                                .setTransition(R.id.startPager, R.id.endUpPager)
                            holder.binding.motionPagerItem.transitionToEnd()

                            lifecycleScope.launch {
                                delay(200)
                                if (this@OneNotePagerFragment
                                        .findNavController()
                                        .currentDestination?.id ==
                                    R.id.oneNotePagerFragment
                                ) {
                                    this@OneNotePagerFragment.findNavController().popBackStack()
                                }
                            }
                        }
                    }

                    override fun onSwipeBottom() {
                        if (!holder.binding.imgOnePhoto.isZoomed) {
                            binding.titleViewOnePhoto.visibility = View.INVISIBLE
                            binding.recyclerInViewPager.visibility = View.INVISIBLE
                            binding.noteViewOnePhoto.visibility = View.INVISIBLE
                            binding.toolbarOneNotePager.visibility = View.INVISIBLE
                            holder.binding.motionPagerItem
                                .setTransition(R.id.startPager, R.id.endDownPager)
                            holder.binding.motionPagerItem.transitionToEnd()

                            lifecycleScope.launch {
                                delay(200)
                                if (this@OneNotePagerFragment
                                        .findNavController()
                                        .currentDestination?.id ==
                                    R.id.oneNotePagerFragment
                                ) {
                                    this@OneNotePagerFragment.findNavController().popBackStack()
                                }
                            }
                        }
                    }

                    //Catch previous state of zoom
                    //It changes after the method returns
                    override fun onDoubleTap() {
                        binding.photoPager.isUserInputEnabled =
                            holder.binding.imgOnePhoto.isZoomed
                    }
                })
        })


        /**
         * Swipe listener to animate image closure
         * @see CustomTouchListener
         *
         * Application uses custom library to zoom image
         * "com.github.MikeOrtiz:TouchImageView:3.0.3"
         */
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireActivity(), R.color.primaryDarkColor)
    }

    private fun crossText(firstNote: FirstNote, textView: TextView) {
        if (firstNote.isChecked) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}