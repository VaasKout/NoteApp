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

class NoteFragment : Fragment() {

    private var actionModeActivate: Boolean = true
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

        binding.toolbarNoteMain.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.delete_from_main -> {
                    viewModel.onClear()
                    true
                } else -> false
            }
        }

        /**
         *  set clickListener in constructor for each item in recyclerView
         */

        val noteAdapter = NoteAdapter(NoteListener(
            {noteId -> viewModel.onNoteClicked(noteId)},
            {checkList, noteId -> viewModel.onInitCheckList(checkList, noteId)}))

        /**
         * set adapter options
         */
        binding.recyclerView.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
            layoutManager =
                StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        }

        /**
         * observes allNotes[LiveData<List<Note>>]
            from view model and makes it equal to notes[listOf<Notes>()] from adapter
         */

        viewModel.allNotes.observe(viewLifecycleOwner, {
            it?.let {
             noteAdapter.submitList(it)
//                noteAdapter.notes = it
            }
        })

        viewModel.navigateToUpdateNoteFragment.observe(viewLifecycleOwner, {noteId ->
            if (viewModel.actionMode == null){
            noteId?.let {
                this.findNavController()
                    .navigate(NoteFragmentDirections
                        .actionNoteFragmentToUpdateNoteFragment(noteId))
                viewModel.onDoneUpdateNavigating()}
            }
        })

        fun destroyActionMode(){
    if (viewModel.allNotes.value?.filter { it.isChecked }?.size == 0){
        viewModel.onDoneActionMode()
        binding.materialButton.visibility = View.VISIBLE
        noteAdapter.notifyDataSetChanged()
        actionModeActivate = true
    }
}

        viewModel.checkedState.observe(viewLifecycleOwner, { state ->

            if (state == true && actionModeActivate){
                binding.materialButton.visibility = View.GONE
                 viewModel.onStartActionMode(requireActivity())
                actionModeActivate = false
            } else if (state == true && !actionModeActivate){
                     viewModel.onResumeActionMode()
                 }
            destroyActionMode()
        })

        noteAdapter.isActive.observe(viewLifecycleOwner, {adapter ->

                adapter.binding.materialCard.setOnClickListener {
                    if (viewModel.actionMode != null){
                    adapter.binding.materialCard.isChecked = !adapter.binding.materialCard.isChecked
                    noteAdapter.currentList[adapter.adapterPosition].isChecked =
                    adapter.binding.materialCard.isChecked
                    viewModel.onResumeActionMode()
                    destroyActionMode()
                    } else if (viewModel.actionMode == null){
                            noteAdapter.click.onClick(noteAdapter
                                .currentList[adapter.adapterPosition])
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