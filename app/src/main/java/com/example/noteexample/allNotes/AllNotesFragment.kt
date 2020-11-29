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
import com.example.noteexample.utils.GlideApp
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AllNotesFragment : Fragment() {

    /**
     *  Define viewModel for NoteFragment
     */

    private val noteAdapter = NoteAdapter()
    var cards = mutableListOf<MaterialCardView>()
    private val viewModel by lazy {
        ViewModelProvider(this).get(AllNotesViewModel::class.java)
    }

    private val actionModeController = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.action_menu, menu)
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
                            lifecycleScope.launch(Dispatchers.Default) {
                                viewModel.onDeleteSelected()
                                withContext(Dispatchers.Main) {
                                    mode?.finish()
                                }
                            }
                        }.show()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            if (viewModel.noteList.any { it.note.isChecked }) {
                viewModel.noteList.forEach { it.note.isChecked = false }
            }
            viewModel.actionModeStarted = false
            viewModel.onDestroyActionMode()
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
            if (viewModel.actionModeStarted) {
                viewModel.onStopActionMode()
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

    //TODO Swipe LEFT, RIGHT
    //TODO Check visibility of img in xml animation for OnePhotoFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentNoteMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_note_main, container, false)
        helper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.apply {
            adapter = noteAdapter
            setHasFixedSize(true)
            if (viewModel.flagsObj?.columns == 1) {
                layoutManager = LinearLayoutManager(requireContext())
            } else if (viewModel.flagsObj?.columns == 2) {
                layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            }
        }


        requireActivity().window
            .addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

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
                        item.note.title.contains(it.toString()) ||
                                item.note.text.contains(it.toString()) ||
                                item.images.any { image -> image.signature == it.toString() }
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

        viewModel.searchMode.observe(viewLifecycleOwner, {
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

        viewModel.flags.observe(viewLifecycleOwner, {
            if (it != null) {
                lifecycleScope.launch {
                    viewModel.flagsObj = it
                    if (it.ascendingOrder) {
                        viewModel.getASCNotes(it.filter)
                    } else {
                        viewModel.getDESCNotes(it.filter)
                    }

//                    binding.recyclerView.apply {
//                        if (adapter == null){
//                            adapter = noteAdapter
//                            setHasFixedSize(true)
//                        }
//                    }

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
                    viewModel.deleteUnused()
                    viewModel.onStopActionMode()
                    cards = mutableListOf()
                    noteAdapter.submitList(viewModel.noteList)
                    noteAdapter.notifyDataSetChanged()
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
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireActivity(), R.color.primaryDarkColor)
            }
        })


        noteAdapter.holder.observe(viewLifecycleOwner, { holder ->
            if (holder.adapterPosition >= 0) {

                lifecycleScope.launch {

                    cards.add(holder.binding.mainCard)

                    val item = noteAdapter.currentList[holder.adapterPosition]
                    val card = holder.binding.mainCard
                    val img = holder.binding.photoMain
                    val title = holder.binding.titleMain
                    val text = holder.binding.noteMain
                    val view1 = holder.binding.view1
                    val view2 = holder.binding.view2
                    val view3 = holder.binding.view3
                    val date = holder.binding.dateMain

                    card.isChecked = false
                    view1.visibility = View.GONE
                    view2.visibility = View.GONE
                    view3.visibility = View.GONE
                    date.visibility = View.GONE
                    img.visibility = View.GONE
                    title.visibility = View.GONE
                    text.visibility = View.GONE


                    if (item.note.title.isNotEmpty()) {
                        title.visibility = View.VISIBLE
                        title.text = item.note.title
                    }

                    if (item.note.text.isNotEmpty()) {
                        text.visibility = View.VISIBLE
                        text.text = item.note.text
                    }

                    if (item.images.isNotEmpty()) {
                        var photoInserted = false
                        for (content in item.images) {
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
                            item.note.text.isEmpty() &&
                            item.images[0].signature.isNotEmpty()
                        ) {
                            text.text = item.images[0].signature
                            text.visibility = View.VISIBLE
                        }

                    }

                    if (item.note.title.isNotEmpty() && item.note.text.isNotEmpty()) {
                        view1.visibility = View.VISIBLE
                    }
                    if ((item.note.text.isNotEmpty() || item.note.title.isNotEmpty()) &&
                        img.visibility == View.VISIBLE
                    ) {
                        view2.visibility = View.VISIBLE
                    }

                    viewModel.flagsObj?.let {
                        if (it.showDate) {
                            date.visibility = View.VISIBLE
                            view3.visibility = View.VISIBLE
                        }
                    }

                    card.setOnLongClickListener {
                        if (viewModel.searchStarted) {
                            viewModel.onDoneSearch()
                        }
                        card.isChecked = !card.isChecked
                        noteAdapter.currentList[holder.adapterPosition].note.isChecked =
                            card.isChecked
                        if (!viewModel.actionModeStarted) {
                            viewModel.onStartActionMode(requireActivity(), actionModeController)
                        } else {
                            viewModel.onResumeActionMode()
                            if (viewModel.noteList.none { it.note.isChecked }) {
                                viewModel.onStopActionMode()
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
                            noteAdapter.currentList[holder.adapterPosition].note.isChecked =
                                card.isChecked
                            viewModel.onResumeActionMode()
                            if (viewModel.noteList.none { it.note.isChecked }) {
                                viewModel.onStopActionMode()
                            }
                        } else if (!viewModel.actionModeStarted) {
                            noteAdapter.currentList[holder.adapterPosition].apply {
                                if (images.size == 1 && images[0].photoPath.isNotEmpty()) {
                                    this@AllNotesFragment.findNavController()
                                        .navigate(
                                            AllNotesFragmentDirections
                                                .actionAllNotesFragmentToOnePhotoFragment
                                                    (note.noteID, images[0].imgID)
                                        )
                                } else {
                                    this@AllNotesFragment.findNavController()
                                        .navigate(
                                            AllNotesFragmentDirections
                                                .actionAllNotesFragmentToOneNoteFragment
                                                    (note.noteID)
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

