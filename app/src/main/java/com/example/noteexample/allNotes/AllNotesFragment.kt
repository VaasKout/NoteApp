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
import com.example.noteexample.databinding.FragmentNoteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AllNotesFragment : Fragment() {
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
                noteAdapter.submitList(it)
                viewModel.noteList = it
                if (viewModel.noteList.any { item -> item.isChecked}){
                    viewModel.onStartActionMode(requireActivity())
                }
            }
        })

        viewModel.allNoteContent.observe(viewLifecycleOwner, {
            viewModel.noteContentList = it
            noteAdapter.notifyDataSetChanged()
        })

        viewModel.navigateToUpdateNoteFragment.observe(viewLifecycleOwner, { noteId ->
            if ( noteId > -1) {
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
         * if not, destroy action mode
         */

        fun checkAndDestroyActionMode() {
            if (viewModel.allNotes.value?.filter { it.isChecked }?.size == 0) {
                binding.materialButton.visibility = View.VISIBLE
                viewModel.actionModeStarted = false
                viewModel.onDestroyActionMode()
            }
        }
        /**
         *
         * [AllNotesViewModel.actionModeStarted] checks if action mode needs to be start again
         */
        viewModel.actionMode.observe(viewLifecycleOwner, { actionMode ->

            if (actionMode != null && !viewModel.actionModeStarted) {
                binding.materialButton.visibility = View.GONE
                viewModel.actionModeStarted = true
            } else if (actionMode != null && viewModel.actionModeStarted) {
                viewModel.onResumeActionMode()
            }

            checkAndDestroyActionMode()
        })

        /**
         * Need another list to observe it in [NoteAdapter.holder] LiveData
         * You can't observe one LiveData in another
         */

        noteAdapter.holder.observe(viewLifecycleOwner, { holder ->
            val card = holder.binding.materialCard
            val item = noteAdapter.currentList[holder.adapterPosition]
            val contentList = viewModel.noteContentList.filter { it.noteId == item.id }
            if (contentList.isNotEmpty()) {
                holder.binding.data = contentList[0]
                Log.e("dataID", "${contentList[0].noteId}")
                Log.e("noteID", "${item.id}")
            }

            if (item.title.isEmpty() &&
                item.firstNote.isEmpty() &&
                contentList.isEmpty()) {
                viewModel.deleteUnused(item, contentList)
            }

            card.setOnLongClickListener {
                item.isChecked = !item.isChecked
                if (!viewModel.actionModeStarted){
                    viewModel.onStartActionMode(requireActivity())
                } else{
                    viewModel.onResumeActionMode()
                    checkAndDestroyActionMode()
                }
                true
            }
            card.setOnClickListener {
                if (viewModel.actionModeStarted) {
                    item.isChecked = !item.isChecked
                    noteAdapter.notifyItemChanged(holder.adapterPosition)
                    viewModel.onResumeActionMode()
                    checkAndDestroyActionMode()
                } else if (!viewModel.actionModeStarted) {
                    viewModel.onNoteClicked(item.id)
                }
            }
        })

        /**
         * listen to fab click
         */

        viewModel.navigateToInsertFragment.observe(viewLifecycleOwner, {
            if (it == true && !viewModel.actionModeStarted) {
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