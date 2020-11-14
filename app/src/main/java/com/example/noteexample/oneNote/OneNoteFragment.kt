package com.example.noteexample.oneNote

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentOneNoteBinding

class OneNoteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args by navArgs<OneNoteFragmentArgs>()
        val binding: FragmentOneNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_one_note, container, false)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = OneNoteViewModelFactory(application, args.noteId)
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(OneNoteViewModel::class.java)

        val oneNoteAdapter = OneNoteViewAdapter()
        binding.recyclerOneNote.apply {
            adapter = oneNoteAdapter
            setHasFixedSize(true)
        }

        viewModel.allNoteContent.observe(viewLifecycleOwner, {allContent ->
            val list = allContent.filter { list -> list.noteId == args.noteId }
            viewModel.currentNote?.let { note ->
                oneNoteAdapter.addHeaderAndSubmitList(note, list)
            }
        })

        /**
         * Menu onClickListener
         */

        binding.toolbarOneNote.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.edit_item -> {
                    this.findNavController()
                        .navigate(OneNoteFragmentDirections
                            .actionOneNoteFragmentToUpdateNoteFragment(args.noteId))
                    true
                }
                else -> false
            }
        }

        // Inflate the layout for this fragment
        binding.lifecycleOwner = this
        return binding.root
    }
}