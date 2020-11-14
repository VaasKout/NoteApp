package com.example.noteexample.allNotes

import android.app.Application
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllNotesViewModel(application: Application) : AndroidViewModel(application) {

    //Flags
    var actionModeStarted = false
    var startedMove = false


    var noteContentList = listOf<NoteContent>()
    var noteList = mutableListOf<Note>()

    private val repository: NoteRepository
    var allSortedNotes: LiveData<List<Note>>
    val allNoteContent: LiveData<List<NoteContent>>

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allSortedNotes = repository.allDESCSortedNotes
        allNoteContent = repository.allNoteContent
    }

    private val _actionMode = MutableLiveData<ActionMode?>()
    var actionMode: LiveData<ActionMode?> = _actionMode

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

    /**
     * LiveData
     */
    private val _navigateToInsertFragment = MutableLiveData<Boolean>()
    val navigateToInsertFragment: LiveData<Boolean> = _navigateToInsertFragment

    private val _navigateToUpdateNoteFragment = MutableLiveData<Int>()
    val navigateToUpdateNoteFragment: LiveData<Int> = _navigateToUpdateNoteFragment

    /**
     * Navigating methods
     */
    fun onStartNavigating() {
        _navigateToInsertFragment.value = true
    }

    fun onDoneEditNavigating() {
        _navigateToInsertFragment.value = false
    }

    fun onNoteClicked(id: Int) {
        _navigateToUpdateNoteFragment.value = id
    }

    fun onDoneUpdateNavigating() {
        _navigateToUpdateNoteFragment.value = -1
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

    /**
     * Coroutine functions
     */

    fun onDeleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val noteContentListToDelete = mutableListOf<NoteContent>()
            val noteListToDelete =
                noteList.filter { it.isChecked }
            noteListToDelete.forEach { note ->
                noteContentListToDelete.addAll(noteContentList
                    .filter { it.noteId == note.id })
            }
            repository.deleteNoteList(noteListToDelete)
            repository.deleteNoteContentList(noteContentListToDelete)
        }
    }

//    fun deleteNote(note: Note) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val contentList = noteContentList.filter { it.noteId == note.id }
//            repository.deleteNoteContentList(contentList)
//            repository.deleteOneNote(note)
//        }
//    }

    fun deleteUnused() {
        viewModelScope.launch(Dispatchers.Default) {
            noteList.forEach { note ->
                val contentList = noteContentList.filter { it.noteId == note.id }
                if (contentList.isEmpty() &&
                    note.title.isEmpty() &&
                    note.firstNote.isEmpty()
                ) {
                    repository.deleteNote(note)
                    repository.deleteNoteContentList(contentList)
                } else if (contentList.isNotEmpty()) {
                    contentList
                        .filter { it.hidden }
                        .forEach {
                            if (it.note.isNotEmpty()) {
                                it.photoPath = ""
                                it.hidden = false
                                repository.updateNoteContent(it)
                            } else {
                                repository.deleteNoteContent(it)
                            }
                        }
                }
            }
        }
    }


    fun updateNoteList() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNoteList(noteList)
        }
    }

    fun onClear() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllNotes()
            repository.deleteAllNoteContent()
        }
    }

    fun swap(from: Int, to: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val fromItem = noteList[from]
            noteList.removeAt(from)
            noteList.add(to, fromItem)
            if (fromItem.pos == from && noteList[to].pos == to) {
                noteList.forEachIndexed { index, note ->
                    note.pos = index
                }
            } else {
                var pos = noteList.size - 1
                noteList.forEach {
                    it.pos = pos
                    pos--
                }
            }
        }
    }
}