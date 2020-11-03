package com.example.noteexample.allNotes

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.*

class AllNotesViewModel(application: Application) : AndroidViewModel(application) {

    //Flags
    var actionModeStarted: Boolean = false

    var noteContentList = listOf<NoteContent>()
    var noteList = listOf<Note>()

    private val repository: NoteRepository
    var allNotes: LiveData<List<Note>>
    val allNoteContent: LiveData<List<NoteContent>>

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
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
            if (allNotes.value?.any { it.isChecked } == true) {
                allNotes.value?.map { it.isChecked = false }
            }
            _actionMode.value = null
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
            "${allNotes.value?.filter { it.isChecked }?.size}"

    }

    fun onResumeActionMode() {
        _actionMode.value?.title =
            "${allNotes.value?.filter { it.isChecked }?.size}"
    }

    fun onDestroyActionMode() {
        _actionMode.value?.finish()
    }

    /**
     * Coroutine functions
     */

    fun onDeleteSelected() {
        viewModelScope.launch {
            allNotes.value?.let { noteList ->
                val deleteNoteList =
                    noteList.filter { it.isChecked }

                allNoteContent.value?.let { content ->
                    Log.e("allContent", allNoteContent.toString())
                    val deleteNoteContentList = mutableListOf<NoteContent>()
                    deleteNoteList.forEach { note ->
                        content
                            .filter { it.noteId == note.id }
                            .forEach {
                                deleteNoteContentList.add(it)
                            }
                    }
                        repository.deleteNoteContentList(deleteNoteContentList)
                }
                repository.deleteNotes(deleteNoteList)
            }
        }
    }

    fun deleteUnused(note: Note, noteContent: List<NoteContent>){
        viewModelScope.launch (Dispatchers.IO) {
            repository.deleteOneNote(note)
            repository.deleteNoteContentList(noteContent)
        }
    }

    fun onClear() {
        viewModelScope.launch {
            repository.deleteAllNotes()
            repository.deleteAllNoteContent()
        }
    }
}