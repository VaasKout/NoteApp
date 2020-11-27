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
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        viewModel.allNoteContent.observe(viewLifecycleOwner, { allContent ->
            lifecycleScope.launch(Dispatchers.Default) {
                viewModel.getNote()
                val list = allContent.filter { list -> list.noteId == args.noteID }
                withContext(Dispatchers.Main){
                    oneNoteAdapter.addHeaderAndSubmitList(viewModel.currentNote, list)
                        //bug when scroll position
                    delay(16)
                    binding.recyclerOneNote
                        .layoutManager?.scrollToPosition(viewModel.scrollPosition)
                }
            }
        })



        oneNoteAdapter.noteContentHolder.observe(viewLifecycleOwner, { holder ->
            oneNoteAdapter.currentList[holder.adapterPosition].noteContent?.let { current ->
                holder.binding.photoOneNote.setOnClickListener {
                    viewModel.scrollPosition = holder.adapterPosition
                    Log.e("holder.pos", holder.adapterPosition.toString())
                    this.findNavController()
                        .navigate(
                            OneNoteFragmentDirections
                                .actionOneNoteFragmentToOnePhotoFragment(args.noteID, current.id)
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