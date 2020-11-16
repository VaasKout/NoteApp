package com.example.noteexample.allNotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AllNotesFragment : Fragment() {

    /**
     *  Define viewModel for NoteFragment
     */


    val viewModel by lazy {
        ViewModelProvider(this).get(AllNotesViewModel::class.java)
    }

    //TODO Filter with photos, without photos
    //TODO Note search in SQL
    //TODO order by old, by recent
    //TODO Date for notes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val cards = mutableListOf<MaterialCardView>()
        val binding: FragmentNoteMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_note_main, container, false)
        binding.noteViewModel = viewModel
        binding.lifecycleOwner = this

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
                    recyclerView.adapter?.notifyItemMoved(from, to)
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
            val img = holder.binding.photoMain
            noteAdapter.currentList[holder.adapterPosition]?.let { current ->

                val contentList = viewModel.noteContentList.filter {
                    it.noteId == current.id
                }

                /**
                 * Set [View.GONE] visibility for [RecyclerMainItemBinding.photoMain] to prevent
                 * bug in [ListAdapter]
                 */

                holder.binding.photoMain.visibility = View.GONE
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

