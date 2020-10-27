package com.example.noteexample.allNotes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteexample.R
import com.example.noteexample.database.NoteContent
import com.example.noteexample.databinding.FragmentNoteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AllNotesFragment : Fragment() {
    private var actionModeNotStarted: Boolean = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_note, container, false)

        /**
         *  define viewModel for NoteFragment
         */

        val viewModel = ViewModelProvider(this).get(AllNotesViewModel::class.java)
        binding.noteViewModel = viewModel

        /**
         * initialize and set adapter options
         */

        val noteAdapter = NoteAdapter()
        binding.recyclerView.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
            layoutManager =
                StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        }

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
                else -> false
            }
        }

        /**
         *  Observes allNotes [AllNotesViewModel.allNotes]
         *  from ViewModel and makes it equal to notes [NoteAdapter.getCurrentList] from adapter
         */

        viewModel.allNotes.observe(viewLifecycleOwner, {
            it?.let {
                //TODO Filter list for RecyclerView
                noteAdapter.submitList(it)
//                noteAdapter.notes = it
            }
        })

        viewModel.navigateToUpdateNoteFragment.observe(viewLifecycleOwner, { noteId ->
            if (viewModel.actionMode == null && noteId != null) {
                this.findNavController()
                    .navigate(
                        AllNotesFragmentDirections
                            .actionAllNotesFragmentToOneNoteFragment(noteId)
                    )
                viewModel.onDoneUpdateNavigating()
            }
        })
        /**
         * Inside function, checks if any notes are checked,
        * if not, destroys action mode
         *
         */
        fun checkAndDestroyActionMode() {
            if (viewModel.allNotes.value?.filter { it.isChecked }?.size == 0) {
                viewModel.onDoneActionMode()
                binding.materialButton.visibility = View.VISIBLE
                actionModeNotStarted = true
                noteAdapter.notifyDataSetChanged()
            }
        }
        /**
         * [AllNotesViewModel.checkedState] checks if action mode is activated
         * [actionModeNotStarted] checks if action mode needs to be start again
         */
        viewModel.checkedState.observe(viewLifecycleOwner, { state ->

            if (state == true && actionModeNotStarted) {
                binding.materialButton.visibility = View.GONE
                viewModel.onStartActionMode(requireActivity())
                actionModeNotStarted = false
            } else if (state == true && !actionModeNotStarted) {
                viewModel.onResumeActionMode()
            }
            checkAndDestroyActionMode()
        })
        /**
         * Need another list to observe it in [NoteAdapter.holder] LiveData
         * You can't observe one LiveData in another
         */
        var list = emptyList<NoteContent>()
        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            list = it
        })

        noteAdapter.holder.observe(viewLifecycleOwner, { adapter ->
            Log.e("visibility", "${adapter.binding.photoMain.visibility}")
            val item = noteAdapter.currentList[adapter.adapterPosition]
            val card = adapter.binding.materialCard
            val contentList = list.filter { it.noteId == item.id }
            if (contentList.isNotEmpty()){
                adapter.binding.data = contentList[0]
                Log.e("dataID", "${contentList[0].noteId}")
                Log.e("noteID", "${item.id}")
            }
            card.setOnLongClickListener {
                card.isChecked = !card.isChecked
                item.isChecked = card.isChecked
                viewModel.onPrepareActionMode()
                true
            }
            card.setOnClickListener {
                if (viewModel.actionMode != null) {
                    card.isChecked = !card.isChecked
                    item.isChecked = card.isChecked
                    viewModel.onResumeActionMode()
                    checkAndDestroyActionMode()
                } else if (viewModel.actionMode == null) {
                    viewModel.onNoteClicked(item.id)
                }
            }
        })

        /**
         * listen to fab click
         */

        viewModel.navigateToInsertFragment.observe(viewLifecycleOwner, {
            if (it == true && viewModel.actionMode == null) {
                this.findNavController()
                    .navigate(
                        AllNotesFragmentDirections
                            .actionNoteFragmentToInsertNoteFragment()
                    )
                viewModel.onDoneEditNavigating()
            }
        })

        binding.lifecycleOwner = this
        return binding.root
    }
}