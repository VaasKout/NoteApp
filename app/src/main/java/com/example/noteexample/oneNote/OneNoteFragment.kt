package com.example.noteexample.oneNote

import android.os.Bundle
import android.util.Log
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
import com.example.noteexample.databinding.FragmentOneNoteBinding
import com.example.noteexample.utils.NoteWithImagesRecyclerItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OneNoteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args by navArgs<OneNoteFragmentArgs>()
        val binding: FragmentOneNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_one_note, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(this.activity).application
        val viewModelFactory = OneNoteViewModelFactory(application, args.noteID)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(OneNoteViewModel::class.java)

        val oneNoteAdapter = OneNoteViewAdapter()
        binding.recyclerOneNote.apply {
            adapter = oneNoteAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.currentNoteLiveData.observe(viewLifecycleOwner, { current ->
            if (viewModel.dataItemList.isEmpty()) {
                viewModel.dataItemList.add(0, NoteWithImagesRecyclerItems(current.note))
                current.images.forEach { image ->
                    viewModel.dataItemList.add(NoteWithImagesRecyclerItems(image = image))
                }
            }

            oneNoteAdapter.submitList(viewModel.dataItemList)
            lifecycleScope.launch {
                delay(2)
                binding.recyclerOneNote
                    .layoutManager?.scrollToPosition(viewModel.scrollPosition)
            }
            //bug when scroll position
        })

        oneNoteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->
            holder.binding.photoOneNote.setOnClickListener {
                viewModel.scrollPosition = holder.adapterPosition
                Log.e("holder.pos", holder.adapterPosition.toString())
                oneNoteAdapter.currentList[holder.adapterPosition].image?.let {
                    this.findNavController()
                        .navigate(
                            OneNoteFragmentDirections
                                .actionOneNoteFragmentToOnePhotoFragment(
                                    args.noteID,
                                    it.imgID
                                )
                        )
                }

            }
        })

        /**
         * Menu onClickListener
         */

        binding.toolbarOneNote.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }

        binding.toolbarOneNote.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_item -> {
                    this.findNavController()
                        .navigate(
                            OneNoteFragmentDirections
                                .actionOneNoteFragmentToEditNoteFragment(args.noteID)
                        )
                    true
                }
                else -> false
            }
        }

        return binding.root
    }
}