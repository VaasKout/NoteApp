package com.example.noteexample.allNotes

import android.app.Application
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.Flags
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllNotesViewModel(application: Application) : AndroidViewModel(application) {

    //Flags
    var actionModeStarted = false
    var startedMove = false
    var searchStarted = false


    var noteContentList = listOf<NoteContent>()
    var noteList = mutableListOf<Note>()
    var flagsObj: Flags? = null

    val flags: LiveData<Flags>

    private val repository: NoteRepository

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        flags = repository.flags
    }

    private val _allSortedNotes = MutableLiveData<List<Note>>()
    val allSortedNotes: LiveData<List<Note>> = _allSortedNotes

    private val _searchMode = MutableLiveData<Boolean>()
    var searchMode: LiveData<Boolean> = _searchMode

    private val _actionMode = MutableLiveData<ActionMode?>()
    var actionMode: LiveData<ActionMode?> = _actionMode

    /**
     * Coroutine functions
     */

    fun getASCNotes(onlyNotes: Boolean, onlyPhotos: Boolean) {
        viewModelScope.launch {
            when {
                onlyNotes -> {
                    _allSortedNotes.value =
                        repository.allASCSortedNotes().filter { !it.hasNoteContent }
                }
                onlyPhotos -> {
                    _allSortedNotes.value =
                        repository.allASCSortedNotes().filter { it.hasNoteContent }
                }
                else -> {
                    _allSortedNotes.value = repository.allASCSortedNotes()
                }
            }
        }
    }

    fun getDESCNotes(onlyNotes: Boolean, onlyPhotos: Boolean) {
        viewModelScope.launch {
            when {
                onlyNotes -> {
                    _allSortedNotes.value =
                        repository.allDESCSortedNotes().filter { !it.hasNoteContent }
                }
                onlyPhotos -> {
                    _allSortedNotes.value =
                        repository.allDESCSortedNotes().filter { it.hasNoteContent }
                }
                else -> {
                    _allSortedNotes.value = repository.allDESCSortedNotes()
                }
            }
        }
    }

    fun onDeleteSelected() {
        val noteContentListToDelete = mutableListOf<NoteContent>()
        val noteListToDelete =
            noteList.filter { it.isChecked }
        noteListToDelete.forEach { note ->
            noteContentListToDelete.addAll(noteContentList
                .filter { it.noteId == note.id })
        }
        viewModelScope.launch {
            repository.deleteNoteList(noteListToDelete)
            repository.deleteNoteContentList(noteContentListToDelete)
            flagsObj?.let { repository.updateFlags(it) }
        }
    }

    suspend fun deleteUnused() {
        noteContentList = repository.allNoteContentSimpleList()
        noteList.forEach { note ->
            val contentList = noteContentList.filter { it.noteId == note.id }
            if (note.title.isEmpty() &&
                note.firstNote.isEmpty() &&
                (contentList.isEmpty() ||
                        contentList.none { !it.hidden || it.note.isNotEmpty() })
            ) {
                repository.deleteNote(note)
                repository.deleteNoteContentList(contentList)
                flagsObj?.let { repository.updateFlags(it) }
            }
        }

        noteContentList.forEachIndexed { index, noteContent ->
            if (noteContent.hidden) {
                if (noteContent.note.isNotEmpty()) {
                    noteContent.photoPath = ""
                    noteContent.hidden = false

                    repository.updateNoteContent(noteContent)
                } else {
                    repository.deleteNoteContent(noteContent)
                }
            }
            if (index == noteContentList.size - 1) {
                noteContentList = repository.allNoteContentSimpleList()
            }
        }
    }

    fun updateNoteList() {
        viewModelScope.launch {
            repository.updateNoteList(noteList)
            flagsObj?.let { repository.updateFlags(it) }
        }
    }

    fun onClear() {
        viewModelScope.launch {
            repository.deleteAllNotes()
            repository.deleteAllNoteContent()
            flagsObj?.let { repository.updateFlags(it) }
        }
    }

    fun swap(from: Int, to: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val fromItem = noteList[from]
            if (fromItem.pos == from && noteList[to].pos == to) {
                noteList.remove(noteList[from])
                noteList.add(to, fromItem)
                noteList.forEachIndexed { index, note ->
                    note.pos = index
                }
            } else {
                noteList.remove(noteList[from])
                noteList.add(to, fromItem)
                var pos = noteList.size - 1
                noteList.forEach {
                    it.pos = pos
                    pos--
                }
            }
        }
    }

    //CallBacks
    private val actionModeController = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.action_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.delete_action -> {
                    onDeleteSelected()
                    mode?.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            viewModelScope.launch(Dispatchers.Default) {
                if (noteList.any { it.isChecked }) {
                    noteList.forEach { it.isChecked = false }
                }
            }

            _actionMode.value = null
            actionModeStarted = false
        }
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
            if (actionModeStarted) {
                onDestroyActionMode()
            }
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition

            if (from >= 0 && to >= 0) {
                swap(from, to)
                recyclerView.adapter?.notifyItemMoved(from, to)
                startedMove = true
            }
            return true
        }

        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            super.clearView(recyclerView, viewHolder)
            if (startedMove) {
                startedMove = false
                updateNoteList()
            }
        }

        override fun onSwiped(
            viewHolder: RecyclerView.ViewHolder,
            direction: Int
        ) {
        }

    })

    fun onStartSearch() {
        _searchMode.value = true
    }

    fun onDoneSearch() {
        _searchMode.value = false
    }


    /**
     * Action mode lifecycle functions
     */

    fun onStartActionMode(activity: FragmentActivity) {
        _actionMode.value = activity.startActionMode(actionModeController)
        _actionMode.value?.title =
            "${noteList.filter { it.isChecked }.size}"
        actionModeStarted = true

    }

    fun onResumeActionMode() {
        _actionMode.value?.title =
            "${noteList.filter { it.isChecked }.size}"
    }

    fun onDestroyActionMode() {
        _actionMode.value?.finish()
    }
}