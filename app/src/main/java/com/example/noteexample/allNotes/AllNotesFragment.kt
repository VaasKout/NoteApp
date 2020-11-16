package com.example.noteexample.allNotes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentNoteMainBinding
import com.example.noteexample.databinding.RecyclerMainItemBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllNotesFragment : Fragment() {

    /**
     *  Define viewModel for NoteFragment
     */


    val viewModel by lazy {
        ViewModelProvider(this).get(AllNotesViewModel::class.java)
    }

    //TODO Filter with photos, without photos
    //TODO order by old, by recent
    //TODO Date for notes
    //TODO Entity of flags in DB
    //TODO end icon in search field
    //TODO backButton close search


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val cards = mutableListOf<MaterialCardView>()
        val binding: FragmentNoteMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_note_main, container, false)


        /**
         * initialize and set adapter options
         */

        val noteAdapter = NoteAdapter()
        binding.recyclerView.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
            layoutManager =
                StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
//            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.helper.attachToRecyclerView(binding.recyclerView)


        binding.toolbarNoteMain.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_from_main -> {
                    if (noteAdapter.currentList.isNotEmpty()) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage("Удалить все заметки?")
                            .setNegativeButton("Нет") { _, _ ->
                            }
                            .setPositiveButton("Да") { _, _ ->
                                viewModel.onClear()
                            }.show()
                    }
                    true
                }
                R.id.search_note -> {
                    if (!viewModel.searchStarted) {
                        viewModel.onStartSearch()
                    } else {
                        viewModel.onDoneSearch()
                    }
                    true
                }
                else -> false
            }
        }

        binding.searchEdit.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                val noteContentList = viewModel.noteContentList.filter { item ->
                    item.note.contains(it.toString())
                }

                val noteList = viewModel.noteList.filter { item ->
                    item.title.contains(it.toString()) ||
                            item.firstNote.contains(it.toString()) ||
                            noteContentList.any { content -> content.noteId == item.id }
                }
                noteAdapter.submitList(noteList)
            } else {
                noteAdapter.submitList(viewModel.noteList)
            }
        }

        viewModel.searchMode.observe(viewLifecycleOwner, {
            if (it) {
                binding.searchEdit.visibility = View.VISIBLE
                binding.searchEdit.setText("")
                viewModel.searchStarted = true
            } else {
                binding.searchEdit.visibility = View.GONE
                binding.searchEdit.setText("")
                noteAdapter.submitList(viewModel.noteList)
                viewModel.searchStarted = false
            }
        })


        /**
         *  Observes allNotes [AllNotesViewModel.allSortedNotes]
         *  from ViewModel and makes it equal to notes [NoteAdapter.getCurrentList] from adapter
         */

        viewModel.allSortedNotes.observe(viewLifecycleOwner, { list ->
            list?.let {
                lifecycleScope.launch(Dispatchers.Default) {
                    viewModel.getNoteContent()
                    viewModel.noteList = mutableListOf()
                    viewModel.noteList.addAll(it)
                    viewModel.deleteUnused()
                    withContext(Dispatchers.Main) {
                        noteAdapter.submitList(viewModel.noteList)
                    }
                    if (viewModel.noteList.any { item -> item.isChecked }) {
                        viewModel.onStartActionMode(requireActivity())
                    }
                }
            }
        })

        /**
         * [AllNotesViewModel.actionModeStarted] checks if action mode needs to be start again
         */
        viewModel.actionMode.observe(viewLifecycleOwner, {
            if (it != null) {
                binding.fabToInsert.visibility = View.GONE
            } else {
                binding.fabToInsert.visibility = View.VISIBLE
                cards.forEach { card ->
                    card.isChecked = false
                }
            }
        })


        noteAdapter.holder.observe(viewLifecycleOwner, { holder ->
            cards.add(holder.binding.mainCard)
            val card = holder.binding.mainCard
            val img = holder.binding.photoMain
            val view1 = holder.binding.view1
            val view2 = holder.binding.view2

            val contentList = viewModel.noteContentList.filter {
                it.noteId == noteAdapter.currentList[holder.adapterPosition].id
            }
            noteAdapter.currentList[holder.adapterPosition]?.let { current ->

                /**
                 * Set [View.GONE] visibility for [RecyclerMainItemBinding.photoMain] to prevent
                 * bug in [ListAdapter]
                 */

                img.visibility = View.GONE
                if (contentList.isNotEmpty()) {
                    for (content in contentList) {
                        if (content.photoPath.isNotEmpty()) {
                            holder.binding.data = content
                            img.visibility = View.VISIBLE
                            break
                        }
                    }
                }

                if (img.visibility == View.GONE
                    && current.firstNote.isEmpty()
                    && contentList.isNotEmpty()
                ) {
                    current.firstNote = contentList[0].note
                    if (current.title.isNotEmpty()) {
                        view1.visibility = View.VISIBLE
                    }
                }

                if (current.title.isNotEmpty() && current.firstNote.isNotEmpty()) {
                    view1.visibility = View.VISIBLE
                }
                if ((current.firstNote.isNotEmpty() || current.title.isNotEmpty()) &&
                    img.visibility == View.VISIBLE
                ) {
                    view2.visibility = View.VISIBLE
                }
            }


            card.setOnLongClickListener {
                if (viewModel.searchStarted) {
                    viewModel.onDoneSearch()
                }
                card.isChecked = !card.isChecked
                noteAdapter.currentList[holder.adapterPosition].isChecked =
                    card.isChecked
                if (!viewModel.actionModeStarted) {
                    viewModel.onStartActionMode(requireActivity())
                } else {
                    viewModel.onResumeActionMode()
                    if (viewModel.noteList.none { it.isChecked }) {
                        viewModel.onDestroyActionMode()
                    }
                }
                true
            }

            card.setOnClickListener {
                if (viewModel.searchStarted) {
                    viewModel.onDoneSearch()
                }
                if (viewModel.actionModeStarted) {
                    card.isChecked = !card.isChecked
                    noteAdapter.currentList[holder.adapterPosition].isChecked =
                        card.isChecked
                    viewModel.onResumeActionMode()
                    if (viewModel.noteList.none { it.isChecked }) {
                        viewModel.onDestroyActionMode()
                    }
                } else if (!viewModel.actionModeStarted) {
                    when {
                        contentList.isEmpty() -> {
                            this.findNavController()
                                .navigate(
                                    AllNotesFragmentDirections
                                        .actionAllNotesFragmentToOnePhotoFragment
                                            (
                                            noteAdapter.currentList[holder.adapterPosition].id,
                                            -1
                                        )
                                )
                        }
                        contentList.size == 1 -> {
                            this.findNavController()
                                .navigate(
                                    AllNotesFragmentDirections
                                        .actionAllNotesFragmentToOnePhotoFragment
                                            (
                                            noteAdapter.currentList[holder.adapterPosition].id,
                                            contentList[0].id
                                        )
                                )
                        }
                        else -> {
                            this.findNavController()
                                .navigate(
                                    AllNotesFragmentDirections
                                        .actionAllNotesFragmentToOneNoteFragment
                                            (noteAdapter.currentList[holder.adapterPosition].id)
                                )
                        }
                    }
                }
            }
        })

        /**
         * listen to fab click
         */
        binding.fabToInsert.setOnClickListener {
            this.findNavController()
                .navigate(
                    AllNotesFragmentDirections
                        .actionNoteFragmentToInsertNoteFragment()
                )
        }
        binding.lifecycleOwner = this
        return binding.root
    }
}

