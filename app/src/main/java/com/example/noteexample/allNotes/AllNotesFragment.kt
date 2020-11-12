package com.example.noteexample.allNotes

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentNoteBinding
import com.example.noteexample.databinding.RecyclerMainItemBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class AllNotesFragment : Fragment() {

    /**
     *  Define viewModel for NoteFragment
     */

    val viewModel by lazy {
        ViewModelProvider(this).get(AllNotesViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val cards = mutableListOf<MaterialCardView>()
        val binding: FragmentNoteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_note, container, false)
        binding.noteViewModel = viewModel
        binding.lifecycleOwner = this
//        var noteListInit = false


        /**
         * initialize and set adapter options
         */

        val noteAdapter = NoteAdapter()
        binding.recyclerView.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
//            layoutManager =
//                StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            layoutManager = LinearLayoutManager(requireContext())
        }


//        var from = 0
//        var to = 0
        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or
                    ItemTouchHelper.DOWN or ItemTouchHelper.UP,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (viewModel.actionModeStarted) {
                    viewModel.onDestroyActionMode()
                }
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                if (from >= 0 && to >= 0) {
                    viewModel.swap(from, to)
                    noteAdapter.notifyItemMoved(from, to)
                    viewModel.startedMove = true
                }
                return true
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                    if (viewModel.startedMove) {
                        viewModel.startedMove = false
                        viewModel.updateNoteList()
                    }
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
            }

        })

        helper.attachToRecyclerView(binding.recyclerView)

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
         *  Observes allNotes [AllNotesViewModel.allSortedNotes]
         *  from ViewModel and makes it equal to notes [NoteAdapter.getCurrentList] from adapter
         */

        viewModel.allSortedNotes.observe(viewLifecycleOwner, { list ->
            list?.let {
                    viewModel.noteList = mutableListOf()
                    viewModel.noteList.addAll(it)
                    noteAdapter.submitList(viewModel.noteList)
                if (viewModel.noteList.any { item -> item.isChecked }) {
                    viewModel.onStartActionMode(requireActivity())
                }
            }
        })

        viewModel.allNoteContent.observe(viewLifecycleOwner, { list ->
            list?.let {
                viewModel.noteContentList = it
                viewModel.deleteUnused()
                noteAdapter.notifyDataSetChanged()
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

        viewModel.navigateToUpdateNoteFragment.observe(viewLifecycleOwner, { noteId ->
            if (noteId > -1) {
                this.findNavController()
                    .navigate(
                        AllNotesFragmentDirections
                            .actionAllNotesFragmentToOneNoteFragment(noteId)
                    )
                viewModel.onDoneUpdateNavigating()
            }
        })

        noteAdapter.holder.observe(viewLifecycleOwner, { holder ->
            cards.add(holder.binding.mainCard)
            val card = holder.binding.mainCard
            noteAdapter.currentList[holder.adapterPosition]?.let { current ->

                val contentList = viewModel.noteContentList.filter {
                    it.noteId == current.id
                }

                /**
                 * Set [View.GONE] visibility for [RecyclerMainItemBinding.photoMain] to prevent
                 * bug in [ListAdapter]
                 */

                //TODO Logic for empty photoPath and not empty note
                holder.binding.photoMain.visibility = View.GONE
                if (contentList.isNotEmpty()) {
                    contentList.forEach {
                        if (it.photoPath.isNotEmpty()) {
                            holder.binding.photoMain.visibility = View.VISIBLE
                            holder.binding.data = it
                            return@forEach
                        }
                    }
                    Log.e("noteID", "${current.id}")
                }
            }

            card.setOnLongClickListener {
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
                if (viewModel.actionModeStarted) {
                    card.isChecked = !card.isChecked
                    noteAdapter.currentList[holder.adapterPosition].isChecked =
                        card.isChecked
                    viewModel.onResumeActionMode()
                    if (viewModel.noteList.none { it.isChecked }) {
                        viewModel.onDestroyActionMode()
                    }
                } else if (!viewModel.actionModeStarted) {
                    viewModel.onNoteClicked(noteAdapter.currentList[holder.adapterPosition].id)
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

        return binding.root
    }
}

