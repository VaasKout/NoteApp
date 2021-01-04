package com.example.noteexample.ui

import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteexample.R
import com.example.noteexample.adapters.NoteAdapter
import com.example.noteexample.databinding.FragmentNoteMainBinding
import com.example.noteexample.utils.GlideApp
import com.example.noteexample.viewmodels.AllNotesViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AllNotesFragment : Fragment() {

    private var actionMode: ActionMode? = null
    private val noteAdapter = NoteAdapter()
    private var cards = mutableListOf<MaterialCardView>()

    /**
     *  Define [AllNotesViewModel] object for [AllNotesFragment]
     */
    private val viewModel: AllNotesViewModel by viewModels()

    /**
     * [actionModeController] is attached to [actionMode] and is called by long click on note
     */

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

    /**
     * [helper] helps to manually sort items on main screen,
     * but sometimes it causes bugs when layout manager is
     * [StaggeredGridLayoutManager] with 2 columns
     */

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
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition

            if (from >= 0 && to >= 0) {
                viewModel.swap(from, to)
                noteAdapter.notifyItemMoved(from, to)
//                if (recyclerView.layoutManager is StaggeredGridLayoutManager){
//                    (recyclerView.layoutManager as StaggeredGridLayoutManager).gapStrategy =
//                        StaggeredGridLayoutManager.GAP_HANDLING_NONE
//                }
//                    if (from == 0 || to == 0){
//                        recyclerView.scrollToPosition(0)
//                        recyclerView.layoutManager?.scrollToPosition(0)
//                    }
//
                viewModel.startedMove = true
            }
            return true
        }

        /**
         * Update [AllNotesViewModel.noteList] when item is released
         * Flag [AllNotesViewModel.startedMove] is used to prevent destruction of action mode
         */
        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            super.clearView(recyclerView, viewHolder)
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
        lifecycleScope.launchWhenCreated {
            requireActivity().window
                .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /**
         * Databinding for [R.layout.fragment_note_main]
         */
        viewModel.onDoneActionMode()
        val binding: FragmentNoteMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_note_main, container, false)

        //RecyclerView options
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


        /**
         * Toolbar clickListener
         */

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
                            AllNotesFragmentDirections.actionAllNotesFragmentToSettingsFragment()
                        )
                    true
                }
                else -> false
            }
        }

        /**
         * Search mode for text in notes
         * Coroutine with [Dispatchers.Default] sort list asynchronously
         * to prevent blocking the ui thread in case of heavy computations
         */
        binding.searchEdit.addTextChangedListener {
            if (viewModel.searchStarted) {
                lifecycleScope.launch(Dispatchers.Default) {
                    val noteList = viewModel.noteList.filter { item ->
                        item.header.title.contains(it.toString()) ||
                                item.notes.any { firstNote ->
                                    firstNote.text.contains(it.toString())
                                } ||
                                item.images.any { image ->
                                    image.signature.contains(it.toString())
                                }
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

        /**
         * [AllNotesViewModel.searchModeFlag] LiveData for searchMode
         */

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

        /**
         * [AllNotesViewModel.flagsLiveData] for [com.example.noteexample.database.Flags]
         */

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
         * [AllNotesViewModel.actionModeFlag] LiveData
         */

        viewModel.actionModeFlag.observe(viewLifecycleOwner, {
            if (it) {
                binding.fabToInsert.visibility = View.GONE
            } else {
                binding.fabToInsert.visibility = View.VISIBLE
            }
        })


        /**
         * [NoteAdapter.holder] LiveData
         * All visibility set manually because of common bug in ListAdapter,
         * Glide and BindingAdapter which set image or title in other items,
         * where it shouldn't be
         */
        noteAdapter.holder.observe(viewLifecycleOwner, { holder ->
            if (holder.absoluteAdapterPosition >= 0) {
                lifecycleScope.launch {
                    cards.add(holder.binding.mainCard)
                    val card = holder.binding.mainCard
                    val title = holder.binding.titleMain
                    val text = holder.binding.noteMain
                    val img = holder.binding.photoMain
                    val view1 = holder.binding.view1
                    val view2 = holder.binding.view2
                    val view3 = holder.binding.view3
                    val date = holder.binding.dateMain

                    card.isChecked = false
                    title.visibility = View.GONE
                    text.visibility = View.GONE
                    img.visibility = View.GONE
                    view1.visibility = View.GONE
                    view2.visibility = View.GONE
                    view3.visibility = View.GONE
                    date.visibility = View.GONE

                    noteAdapter.currentList[holder.absoluteAdapterPosition]?.also {
                        if (it.header.title.isNotEmpty()) {
                            title.visibility = View.VISIBLE
                            title.text = it.header.title
                        }

                        //TODO change to list
//                        if (it.header.text.isNotEmpty()) {
//                            text.visibility = View.VISIBLE
//                            text.text = it.header.text
//                        }

                        if (it.images.isNotEmpty()) {
                            var photoInserted = false
                            for (content in it.images) {
                                if (content.photoPath.isNotEmpty()) {
                                    img.visibility = View.VISIBLE
                                    GlideApp.with(requireContext())
                                        .load(content.photoPath)
                                        .into(img)
                                    photoInserted = true
                                    break
                                }
                            }

                            if (!photoInserted &&
                                it.notes.isEmpty() &&
                                it.images[0].signature.isNotEmpty()
                            ) {
                                text.text = it.images[0].signature
                                text.visibility = View.VISIBLE
                            }
                        }

                        if (it.header.title.isNotEmpty() && it.notes.isNotEmpty()) {
                            view1.visibility = View.VISIBLE
                        }
                        if ((it.notes.isNotEmpty() || it.header.title.isNotEmpty()) &&
                            img.visibility == View.VISIBLE
                        ) {
                            view2.visibility = View.VISIBLE
                        }

                        viewModel.flags?.let { flags ->
                            if (flags.showDate) {
                                date.text = it.header.date
                                date.visibility = View.VISIBLE
                                view3.visibility = View.VISIBLE
                            }
                        }
                    }


                    //MainCard LongCLickListener
                    //Long click starts actionMode
                    card.setOnLongClickListener {
                        if (viewModel.searchStarted) {
                            viewModel.onDoneSearch()
                        }
                        card.isChecked = !card.isChecked
                        noteAdapter.currentList[holder.absoluteAdapterPosition].header.isChecked =
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

                    //MainCard ClickListener
                    card.setOnClickListener {
                        if (viewModel.searchStarted) {
                            viewModel.onDoneSearch()
                        }
                        if (actionMode != null) {
                            card.isChecked = !card.isChecked
                            noteAdapter.currentList[holder.absoluteAdapterPosition].header.isChecked =
                                card.isChecked
                            actionMode?.title =
                                "${viewModel.noteList.filter { it.header.isChecked }.size}"
                            if (viewModel.noteList.none { it.header.isChecked }) {
                                actionMode?.finish()
                            }
                        } else {
                            noteAdapter.currentList[holder.absoluteAdapterPosition].apply {
                                if (this@AllNotesFragment
                                        .findNavController()
                                        .currentDestination?.id ==
                                    R.id.allNotesFragment
                                ) {
                                    if (images.size == 1 &&
                                        images[0].photoPath.isNotEmpty()
                                    ) {
                                        this@AllNotesFragment.findNavController()
                                            .navigate(
                                                AllNotesFragmentDirections
                                                    .actionAllNotesFragmentToOnePhotoFragment(
                                                        header.headerID,
                                                    )
                                            )
                                    } else {
                                        this@AllNotesFragment.findNavController()
                                            .navigate(
                                                AllNotesFragmentDirections
                                                    .actionAllNotesFragmentToOneNoteFragment(header.headerID)
                                            )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })

        /**
         * listen to fab click and goto [EditNoteFragment]
         */
        binding.fabToInsert.setOnClickListener {
            this.findNavController()
                .navigate(
                    AllNotesFragmentDirections.actionAllNotesFragmentToEditNoteFragment()
                )
            viewModel.onDoneSearch()
        }

        /**
         * Convenient back press to end [actionMode] or [AllNotesViewModel.searchModeFlag]
         * and if none of them aren't activated, then close app
         */
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (viewModel.searchStarted) {
                viewModel.onDoneSearch()
            } else {
                requireActivity().finishAffinity()
            }
        }

        //LifecycleOwner for LiveData
        binding.lifecycleOwner = this
        return binding.root
    }
}