package com.example.noteexample.ui

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentOneNoteBinding
import com.example.noteexample.viewmodels.OneNoteViewModel
import com.example.noteexample.adapters.ViewNoteAdapter
import com.example.noteexample.database.FirstNote
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.viewmodels.NoteViewModelFactory
import com.google.android.material.checkbox.MaterialCheckBox
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OneNoteFragment : Fragment() {


    @Inject
    lateinit var repository: NoteRepository
    lateinit var binding: FragmentOneNoteBinding
    private val args by navArgs<OneNoteFragmentArgs>()
    private val oneNoteAdapter = ViewNoteAdapter()
    private val viewModel: OneNoteViewModel by viewModels {
        NoteViewModelFactory(args.noteID, repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        /**
         * Binding for OneNoteFragment
         * @see R.layout.fragment_one_note
         */

        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_one_note, container, false)
        binding.lifecycleOwner = this

        //Adapter options

        binding.recyclerOneNote.apply {
            adapter = oneNoteAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }


        viewModel.currentNoteLiveData.observe(viewLifecycleOwner, { current ->
            lifecycleScope.launch {
                viewModel.currentNote = current
                viewModel.createNoteList()

                oneNoteAdapter.submitList(viewModel.dataItemList)

                /**
                 * Scroll to specific position when user returns from [OneNotePagerFragment]
                 * by default it gets to 0, so user can have bad experience
                 */
                if (viewModel.scrollPosition > 0) {
                    delay(96)
                    binding.recyclerOneNote.smoothScrollToPosition(viewModel.scrollPosition)
                    viewModel.scrollPosition = 0
                }
            }
        })

        /**
         * goto [OneNotePagerFragment] with specific photo
         */
        oneNoteAdapter.firstNoteTodoHolder.observe(viewLifecycleOwner, { holder ->
            oneNoteAdapter.currentList[holder.absoluteAdapterPosition].firstNote?.also {
                crossText(it, holder.binding.checkboxView)
                holder.binding.checkboxView.text = it.text
                holder.binding.checkboxView.isChecked = it.isChecked
            }
                holder.binding.checkboxView.setOnCheckedChangeListener { _, isChecked ->
                    oneNoteAdapter.currentList[holder.absoluteAdapterPosition].firstNote?.let {
                            note ->
                        note.isChecked = isChecked
                        viewModel.updateFirstNote(note)
                        crossText(note, holder.binding.checkboxView)
                    }
                }

        })

        oneNoteAdapter.imgHolder.observe(viewLifecycleOwner, { holder ->
            holder.binding.photoOneNote.setOnClickListener {
                viewModel.scrollPosition = holder.absoluteAdapterPosition
                oneNoteAdapter.currentList[holder.absoluteAdapterPosition].image?.let {
                    this.findNavController()
                        .navigate(
                            OneNoteFragmentDirections.actionOneNoteFragmentToOnePhotoFragment(
                                args.noteID,
                                viewModel.scrollPosition - 2
                            )
                        )
                }
            }
        })

        /**
         * NavIcon clickListener
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

    private fun crossText(firstNote: FirstNote, edit: MaterialCheckBox) {
        if (firstNote.isChecked) {
            edit.paintFlags = edit.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            edit.paintFlags = edit.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}