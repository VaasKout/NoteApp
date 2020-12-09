package com.example.noteexample.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteexample.R
import com.example.noteexample.adapters.OneNoteViewAdapter
import com.example.noteexample.databinding.FragmentOneNoteBinding
import com.example.noteexample.viewmodels.OneNoteViewModel
import com.example.noteexample.adapters.NoteWithImagesRecyclerItems
import com.example.noteexample.viewmodels.NoteViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OneNoteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /**
         * Binding for OneNoteFragment
         * @see R.layout.fragment_one_note
         */
        val args by navArgs<OneNoteFragmentArgs>()
        val binding: FragmentOneNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_one_note, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = NoteViewModelFactory(args.noteID, application)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(OneNoteViewModel::class.java)

        //Adapter options
        val oneNoteAdapter = OneNoteViewAdapter()
        binding.recyclerOneNote.apply {
            adapter = oneNoteAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }


        viewModel.currentNoteLiveData.observe(viewLifecycleOwner, { current ->
            if (viewModel.dataItemList.isEmpty()) {
                viewModel.dataItemList.add(0, NoteWithImagesRecyclerItems(current.header))
                current.images.forEach { image ->
                    viewModel.dataItemList.add(NoteWithImagesRecyclerItems(image = image))
                }
            }
            oneNoteAdapter.submitList(viewModel.dataItemList)

            /**
             * Scroll to specific position when user returns from [OnePhotoFragment]
             * by default it gets to 0, so user can have bad experience
             */
            lifecycleScope.launch {
                //it doesn't work without delay
                delay(8)
                binding.recyclerOneNote
                    .layoutManager?.scrollToPosition(viewModel.scrollPosition)
            }
        })

        /**
         * goto [OnePhotoFragment] with specific photo
         */
        oneNoteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->
            holder.binding.photoOneNote.setOnClickListener {
                viewModel.scrollPosition = holder.adapterPosition
                oneNoteAdapter.currentList[holder.adapterPosition].image?.let {
                    this.findNavController()
                        .navigate(
                            OneNoteFragmentDirections.actionOneNoteFragmentToOnePhotoFragment(
                                args.noteID,
                                it.imgID
                            )
                        )
                }
            }
        })

        /**
         * Nav Icon clickListener
         */
        binding.toolbarOneNote.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }

        /**
         * Menu clickListener
         */
        binding.toolbarOneNote.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_item -> {
                    this.findNavController()
                        .navigate(
                            OneNoteFragmentDirections.actionOneNoteFragmentToEditNoteFragment(args.noteID)
                        )
                    true
                }
                else -> false
            }
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}