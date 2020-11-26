package com.example.noteexample.allNotes

import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentNoteMainBinding
import com.example.noteexample.databinding.RecyclerMainItemBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllNotesFragment : Fragment() {

    /**
     *  Define viewModel for NoteFragment
     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(AllNotesViewModel::class.java)
        val cards = mutableListOf<MaterialCardView>()
        val binding: FragmentNoteMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_note_main, container, false)

        /**
         * initialize and set adapter options
         */

        requireActivity().window
            .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val noteAdapter = NoteAdapter()
        binding.recyclerView.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
        }
        var layoutManager: RecyclerView.LayoutManager? = null

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
                R.id.settings -> {
                    this.findNavController()
                        .navigate(
                            AllNotesFragmentDirections
                                .actionAllNotesFragmentToSettingsFragment()
                        )
                    true
                }
                else -> false
            }
        }

        binding.searchEdit.addTextChangedListener {
            lifecycleScope.launch(Dispatchers.Default) {
                val noteContentList = viewModel.noteContentList.filter { item ->
                    item.note.contains(it.toString(), true)
                }
                val noteList = viewModel.noteList.filter { item ->
                    item.title.contains(it.toString()) ||
                            item.firstNote.contains(it.toString()) ||
                            noteContentList.any { content -> content.noteId == item.id }
                }
                withContext(Dispatchers.Main) {
                    if (it.toString().isEmpty()) {
                        noteAdapter.submitList(viewModel.noteList)
                    } else {
                        noteAdapter.submitList(noteList)
                    }
                }
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

        viewModel.flags.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.flagsObj = it
                if (it.ascendingOrder) {
                    viewModel.getASCNotes(it.filter)

                } else {
                    viewModel.getDESCNotes(it.filter)
                }

                if (it.columns == 2 &&
                    layoutManager !is StaggeredGridLayoutManager
                ) {
                    layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
                    binding.recyclerView.layoutManager = layoutManager
                } else if (it.columns == 1 &&
                    layoutManager !is LinearLayoutManager
                ) {
                    layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerView.layoutManager = layoutManager
                }
                lifecycleScope.launch {
                    delay(32)
                    viewModel.scrollPosition?.let { pos ->
                        binding.recyclerView.layoutManager?.scrollToPosition(pos)
                    }
                    viewModel.scrollPosition = null
                    noteAdapter.notifyDataSetChanged()
                }
            }
        })


        /**
         *  Observes allNotes [AllNotesViewModel.allSortedNotes]
         *  from ViewModel and makes it equal to notes [NoteAdapter.getCurrentList] from adapter
         */

        viewModel.allSortedNotes.observe(viewLifecycleOwner, {
            if (it != null) {
                lifecycleScope.launch(Dispatchers.Default) {
                    viewModel.noteList = mutableListOf()
                    viewModel.noteList.addAll(it)
                    viewModel.deleteUnused()
                    withContext(Dispatchers.Main) {
                        viewModel.onDestroyActionMode()
                        noteAdapter.submitList(viewModel.noteList)
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
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireActivity(), R.color.grey_material)
            } else {
                binding.fabToInsert.visibility = View.VISIBLE
                cards.forEach { card ->
                    card.isChecked = false
                }
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireActivity(), R.color.primaryDarkColor)
            }
        })


        noteAdapter.holder.observe(viewLifecycleOwner, { holder ->
            cards.add(holder.binding.mainCard)
            val card = holder.binding.mainCard
            val img = holder.binding.photoMain
            val view1 = holder.binding.view1
            val view2 = holder.binding.view2
            val view3 = holder.binding.view3
            val noteMain = holder.binding.noteMain
            val date = holder.binding.dateMain

            noteMain.visibility = View.GONE
            img.visibility = View.GONE
            date.visibility = View.GONE
            view1.visibility = View.GONE
            view2.visibility = View.GONE
            view3.visibility = View.GONE

            viewModel.flagsObj?.let {
                if (it.showDate) {
                    date.visibility = View.VISIBLE
                    view3.visibility = View.VISIBLE
                }
            }

            val contentList = viewModel.noteContentList.filter {
                it.noteId == noteAdapter.currentList[holder.adapterPosition].id
            }

            noteAdapter.currentList[holder.adapterPosition]?.let { current ->

                /**
                 * Set [View.GONE] visibility for [RecyclerMainItemBinding.photoMain] to prevent
                 * bug in [ListAdapter]
                 */

                if (contentList.isNotEmpty()) {
                    for (content in contentList) {
                        if (content.photoPath.isNotEmpty()) {
                            holder.binding.data = content
                            img.visibility = View.VISIBLE
                            break
                        }
                    }
                }

                if (current.firstNote.isNotEmpty()) {
                    noteMain.visibility = View.VISIBLE
                    noteMain.text = current.firstNote
                }

                if (img.visibility == View.GONE
                    && current.firstNote.isEmpty()
                    && contentList.isNotEmpty()
                ) {
                    noteMain.text = contentList[0].note
                    noteMain.visibility = View.VISIBLE
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
                    viewModel.onStartActionMode(requireActivity(), requireContext())
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
                    viewModel.scrollPosition = holder.adapterPosition
                    if (contentList.size == 1 && contentList[0].photoPath.isNotEmpty()) {
                        this.findNavController()
                            .navigate(
                                AllNotesFragmentDirections
                                    .actionAllNotesFragmentToOnePhotoFragment
                                        (
                                        noteAdapter.currentList[holder.adapterPosition].id,
                                        contentList[0].id
                                    )
                            )
                    } else {
                        this.findNavController()
                            .navigate(
                                AllNotesFragmentDirections
                                    .actionAllNotesFragmentToOneNoteFragment
                                        (noteAdapter.currentList[holder.adapterPosition].id)
                            )
                    }
                }
            }
        })

        /**
         * listen to fab click
         */
        binding.fabToInsert.setOnClickListener {
            viewModel.flagsObj?.let {
                if (it.ascendingOrder) {
                    viewModel.scrollPosition = viewModel.noteList.size
                } else {
                    viewModel.scrollPosition = 0
                }
            }

            this.findNavController()
                .navigate(
                    AllNotesFragmentDirections
                        .actionAllNotesFragmentToEditNoteFragment()
                )
            viewModel.onDoneSearch()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (viewModel.searchStarted) {
                viewModel.onDoneSearch()
            } else {
                requireActivity().finishAffinity()
            }
        }

        binding.lifecycleOwner = this
        return binding.root
    }
}

