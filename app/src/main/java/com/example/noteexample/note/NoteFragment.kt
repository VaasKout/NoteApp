package com.example.noteexample.note

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentNoteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NoteFragment : Fragment() {

    private var actionModeOnResume: Boolean = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding : FragmentNoteBinding=
            DataBindingUtil.inflate(inflater, R.layout.fragment_note, container, false)

        /**
         *  define viewModel for NoteFragment
         */
         val viewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        binding.noteViewModel = viewModel

        val noteAdapter = NoteAdapter()
        binding.recyclerView.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
            layoutManager =
                StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        }


        binding.toolbarNoteMain.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.delete_from_main -> {
                    if (noteAdapter.currentList.isNotEmpty()){
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage("Удалить все заметки?")
                            .setNegativeButton("Нет") { _, _ ->
                            }
                            .setPositiveButton("Да") { _, _ ->
                                viewModel.onClear()
                            }.show()
                    }
                    true
                } else -> false
            }
        }

        /**
         * initialize and set adapter options
         */



        /**
         *  Observes allNotes [NoteViewModel.allNotes]
            from view model and makes it equal to notes[listOf<Notes>()] from adapter
         */

        viewModel.allNotes.observe(viewLifecycleOwner, {
            it?.let {
             noteAdapter.submitList(it)
//                noteAdapter.notes = it
            }
        })

        viewModel.navigateToUpdateNoteFragment.observe(viewLifecycleOwner, {noteId ->
            if (viewModel.actionMode == null && noteId != null){
                    this.findNavController()
                        .navigate(NoteFragmentDirections
                            .actionNoteFragmentToUpdateNoteFragment(noteId))
                    viewModel.onDoneUpdateNavigating()
            }
        })
        /**
         * Inside function, checks if any notes are checked,
         * if not
         *
         */
        fun checkAndDestroyActionMode(){
    if (viewModel.allNotes.value?.filter { it.isChecked }?.size == 0){
        viewModel.onDoneActionMode()
        binding.materialButton.visibility = View.VISIBLE
        noteAdapter.notifyDataSetChanged()
        actionModeOnResume = true
    }
}
        /**
         * [NoteViewModel.checkedState] checks if action mode is activated
         * [actionModeOnResume] checks if action mode needs to be start again
         */
        viewModel.checkedState.observe(viewLifecycleOwner, { state ->

            if (state == true && actionModeOnResume){
                binding.materialButton.visibility = View.GONE
                viewModel.onStartActionMode(requireActivity())
                actionModeOnResume = false
            } else if (state == true && !actionModeOnResume){
                     viewModel.onResumeActionMode()
                 }
            checkAndDestroyActionMode()
        })

        noteAdapter.isActive.observe(viewLifecycleOwner, {adapter ->
            val item = noteAdapter.currentList[adapter.adapterPosition]
            val card = adapter.binding.materialCard
            adapter.binding.materialCard.setOnLongClickListener {
                card.isChecked = !card.isChecked
                item.isChecked = card.isChecked
                viewModel.onInitCheckList(item.isChecked, item.id)
                true
            }
                adapter.binding.materialCard.setOnClickListener {
                    if (viewModel.actionMode != null){
                    card.isChecked = !card.isChecked
                    item.isChecked = card.isChecked
                    viewModel.onResumeActionMode()
                    checkAndDestroyActionMode()
                    } else if (viewModel.actionMode == null){
                           viewModel.onNoteClicked(item.id)
                    }
                }
            })


        /**
         * listen to fab click
         */
        viewModel.navigateToEditNoteFragment.observe(viewLifecycleOwner, {
            if (it == true && viewModel.actionMode == null){
                this.findNavController()
                    .navigate(NoteFragmentDirections.actionNoteFragmentToEditNoteFragment())
                viewModel.onDoneEditNavigating()
            }
        })


        binding.lifecycleOwner = this
        return binding.root
    }
}