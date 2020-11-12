package com.example.noteexample.insertNote

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InsertNoteViewModel(application: Application) : AndroidViewModel(application) {

    //Flags
    var backPressed = false
    var allHidden = true
//    var secondNoteInit = false

    //Repository
    private val repository: NoteRepository

    //Variables
//    var secondNote = ""
//    var noteContentToDelete: NoteContent? = null
    var title = ""
    var firstNote = ""
    var note: Note? = null
    var size = 0

    var noteContentList = listOf<NoteContent>()


    //Live Data
    private val _navigateToNoteFragment = MutableLiveData<Boolean>()
    val navigateToNoteFragment: LiveData<Boolean> = _navigateToNoteFragment
    val allNotes: LiveData<List<Note>>
    val allNoteContent: LiveData<List<NoteContent>>



    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
        allNotes = repository.allASCSortedNotes
        insertNote()
    }

    /**
     * functions for navigating
     */

    fun onStartNavigating() {
        _navigateToNoteFragment.value = true
    }

    fun onDoneNavigating() {
        _navigateToNoteFragment.value = false
        backPressed = false
    }

    /**
     * Coroutine methods
     */

//    fun updateNoteContent(noteContent: NoteContent) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.updateNoteContent(noteContent)
//        }
//    }


    fun insertCameraPhoto(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            note?.let {
                val localList = noteContentList.filter { list -> list.hidden }
                if (localList.isEmpty()) {
                    val noteContent = NoteContent(
                        noteId = it.id,
                        photoPath = path
                    )
                    repository.insertNoteContent(noteContent)
                } else {
                    localList.forEach { item ->
                        if (item.hidden) {
                            item.photoPath = path
                            item.hidden = false
                            Log.e("it.note", item.note)
                            repository.updateNoteContent(item)
                            return@forEach
                        }
                    }
                }
            }
        }
    }

    fun insertNote() {
        viewModelScope.launch {
            note = Note()
            note?.let {
                repository.insertNote(it)
            }
            note = repository.getLastNote()
        }
    }

    fun updateNoteContentList(noteContent: List<NoteContent>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNoteContentList(noteContent)
        }
    }


    fun updateCurrentNote(title: String = "", firstNote: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            note?.let {
                it.title = title
                it.firstNote = firstNote
                it.pos = size
                repository.updateNote(it)
            }
        }
    }

    fun updateHidden() {
        viewModelScope.launch(Dispatchers.IO) {
            noteContentList.forEach {
                if (it.hidden) {
                    if (it.note.isNotEmpty()) {
                        it.photoPath = ""
                        repository.updateNoteContent(it)
                    } else {
                        repository.deleteNoteContent(it)
                    }
                }
            }
        }
    }

    fun deleteUnused() {
        viewModelScope.launch(Dispatchers.IO) {
            note?.let { repository.deleteNote(it) }
        }
    }

//    fun deleteNoteContent(noteContent: NoteContent) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.deleteNoteContent(noteContent)
//        }
//    }
}