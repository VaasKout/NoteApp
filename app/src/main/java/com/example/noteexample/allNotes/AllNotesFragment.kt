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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteexample.R
import com.example.noteexample.databinding.FragmentNoteMainBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllNotesFragment : Fragment() {

    /**
     *  Define viewModel for NoteFragment
     */
    private var actionMode: ActionMode? = null
    private val noteAdapter = NoteAdapter()
    private var cards = mutableListOf<MaterialCardView>()
    private val viewModel by lazy {
        ViewModelProvider(this).get(AllNotesViewModel::class.java)
    }

    private val actionModeController = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            viewModel.onStartActionMode()
            mode?.menuInflater?.inflate(R.menu.action_menu, menu)
            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireActivity(), R.color.grey_material)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.delete_action -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage("Удалить выбранное?")
                        .setNegativeButton("Нет") { _, _ ->
                        }
                        .setPositiveButton("Да") { _, _ ->
                            viewModel.onDeleteSelected()
                            mode?.finish()
                        }.show()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            viewModel.onDoneActionMode()
            actionMode = null
            activity?.window?.statusBarColor =
                ContextCompat.getColor(requireActivity(), R.color.primaryDarkColor)
            if (viewModel.noteList.any { it.header.isChecked }) {
                viewModel.noteList.forEach { it.header.isChecked = false }
            }
            cards.forEach { card ->
                card.isChecked = false
            }
        }
    }

    private val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or
                ItemTouchHelper.DOWN or ItemTouchHelper.UP,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            if (actionMode != null) {
                actionMode?.finish()
            }
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition

            if (from >= 0 && to >= 0) {
                viewModel.swap(from, to)
                noteAdapter.notifyItemMoved(from, to)
//                if (recyclerView.layoutManager is StaggeredGridLayoutManager){
//                    (recyclerView.layoutManager as StaggeredGridLayoutManager).gapStrategy =
//                        StaggeredGridLayoutManager.GAP_HANDLING_NONE
//                }
//                lifecycleScope.launch {
//                    delay(8)
//                    if (from == 0 || to == 0){
//                        recyclerView.scrollToPosition(0)
//                        recyclerView.layoutManager?.scrollToPosition(0)
//                    }
//                }

                viewModel.startedMove = true
            }
            return true
        }

        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            super.clearView(recyclerView, viewHolder)
            //Started move to not destroy action mode
            if (viewModel.startedMove) {
//                if (recyclerView.layoutManager is StaggeredGridLayoutManager){
//                    (recyclerView.layoutManager as StaggeredGridLayoutManager).gapStrategy =
//                        StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
//                }
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

    init {
        lifecycleScope.launchWhenStarted {
            requireActivity().window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.onDoneActionMode()
        val binding: FragmentNoteMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_note_main, container, false)
        binding.recyclerView.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
            if (viewModel.flags?.columns == 1) {
                layoutManager = LinearLayoutManager(requireContext())
            } else if (viewModel.flags?.columns == 2) {
                layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            }
        }
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
            if (viewModel.searchStarted) {
                lifecycleScope.launch(Dispatchers.Default) {
                    val noteList = viewModel.noteList.filter { item ->
                        item.header.title.contains(it.toString()) ||
                                item.header.text.contains(it.toString()) ||
                                item.images.any { image -> image.signature.contains(it.toString()) }
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
        }

        viewModel.searchModeFlag.observe(viewLifecycleOwner, {
            if (it) {
                binding.searchEdit.visibility = View.VISIBLE
                binding.searchEdit.setText("")
                viewModel.searchStarted = true
            } else {
                binding.searchEdit.visibility = View.GONE
                binding.searchEdit.setText("")
                viewModel.searchStarted = false
            }
        })

        viewModel.flagsLiveData.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.flags = it
                lifecycleScope.launch {
                    if (it.ascendingOrder) {
                        viewModel.getASCNotes(it.filter)
                    } else {
                        viewModel.getDESCNotes(it.filter)
                    }
                    viewModel.deleteUnused()
                    cards = mutableListOf()
                    noteAdapter.submitList(viewModel.noteList)
                    noteAdapter.notifyDataSetChanged()

                }

                if (it.columns == 2 &&
                    binding.recyclerView.layoutManager !is StaggeredGridLayoutManager
                ) {
                    binding.recyclerView.layoutManager =
                        StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
                } else if (it.columns == 1 &&
                    binding.recyclerView.layoutManager !is LinearLayoutManager
                ) {
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }
        })


        /**
         * [AllNotesViewModel.actionModeFlag]
         */
        viewModel.actionModeFlag.observe(viewLifecycleOwner, {
            if (it) {
                binding.fabToInsert.visibility = View.GONE
            } else {
                binding.fabToInsert.visibility = View.VISIBLE
            }
        })


        noteAdapter.holder.observe(viewLifecycleOwner, { holder ->
            if (holder.adapterPosition >= 0) {
                lifecycleScope.launch {
                    cards.add(holder.binding.mainCard)
                    val card = holder.binding.mainCard
                    val date = holder.binding.dateMain
                    val view3 = holder.binding.view3

                    viewModel.flags?.let {
                        if (it.showDate) {
                            date.visibility = View.VISIBLE
                            view3.visibility = View.VISIBLE
                        } else{
                            date.visibility = View.GONE
                        }
                    }

                    card.setOnLongClickListener {
                        if (viewModel.searchStarted) {
                            viewModel.onDoneSearch()
                        }
                        card.isChecked = !card.isChecked
                        noteAdapter.currentList[holder.adapterPosition].header.isChecked =
                            card.isChecked
                        if (actionMode == null) {
                            actionMode =
                                requireActivity().startActionMode(actionModeController)
                            actionMode?.title =
                                "${viewModel.noteList.filter { it.header.isChecked }.size}"
                        } else {
                            actionMode?.title =
                                "${viewModel.noteList.filter { it.header.isChecked }.size}"
                            if (viewModel.noteList.none { it.header.isChecked }) {
                                actionMode?.finish()
                            }
                        }
                        true
                    }

                    card.setOnClickListener {
                        if (viewModel.searchStarted) {
                            viewModel.onDoneSearch()
                        }
                        if (actionMode != null) {
                            card.isChecked = !card.isChecked
                            noteAdapter.currentList[holder.adapterPosition].header.isChecked =
                                card.isChecked
                            actionMode?.title =
                                "${viewModel.noteList.filter { it.header.isChecked }.size}"
                            if (viewModel.noteList.none { it.header.isChecked }) {
                                actionMode?.finish()
                            }
                        } else {
                            noteAdapter.currentList[holder.adapterPosition].apply {
                                if (images.size == 1 && images[0].photoPath.isNotEmpty()) {
                                    this@AllNotesFragment.findNavController()
                                        .navigate(
                                            AllNotesFragmentDirections
                                                .actionAllNotesFragmentToOnePhotoFragment
                                                    (header.noteID, images[0].imgID)
                                        )
                                } else {
                                    this@AllNotesFragment.findNavController()
                                        .navigate(
                                            AllNotesFragmentDirections
                                                .actionAllNotesFragmentToOneNoteFragment
                                                    (header.noteID)
                                        )
                                }
                            }
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