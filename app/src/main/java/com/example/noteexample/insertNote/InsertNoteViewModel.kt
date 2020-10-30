package com.example.noteexample.insertNote

import android.app.Application
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
    private var noteInserted = false
    var backPressed = false
    var secondNoteInit = false

    //Repository
    private val repository: NoteRepository

    //Variables
    var title = ""
    var firstNote = ""
    var secondNote = ""
    var note: Note? = null
    var noteContentToDelete: NoteContent? = null
    val noteContentList = mutableListOf<NoteContent>()

    //Live Data
    private val _navigateToNoteFragment = MutableLiveData<Boolean>()
    val navigateToNoteFragment: LiveData<Boolean> = _navigateToNoteFragment
    val allNoteContent: LiveData<List<NoteContent>>


    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
        updateCurrentNote()
    }

    /**
     * functions for navigating
     */

    fun onStartNavigating() {
        _navigateToNoteFragment.value = true
    }

    fun onDoneNavigating() {
        _navigateToNoteFragment.value = false
    }

    /**
     * Coroutine methods
     */

    fun updateNoteContentList(noteContent: List<NoteContent>){
        viewModelScope.launch (Dispatchers.IO){
            repository.updateNoteContentList(noteContent)
        }
    }

    fun updateNoteContent(noteContent: NoteContent){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNoteContent(noteContent)
        }
    }

    fun insertPhoto(path: String){
        viewModelScope.launch (Dispatchers.IO) {
            note?.let {
                val noteContent = NoteContent(
                    noteId = it.id,
                    photoPath = path
                )
                repository.insertNoteContent(noteContent)
            }
        }
    }

     fun updateCurrentNote(title: String = "", firstNote: String = "") {
        viewModelScope.launch (Dispatchers.IO) {
            if (!noteInserted){
                note = Note()
                note?.let {
                    repository.insertNote(it)
                    var newNote: Note? = null
                    while (newNote == null){
                        newNote = repository.getLastNote()
                        note = newNote
                    }
                }
                noteInserted = true
            }
            note?.let {
                    it.title = title
                    it.firstNote = firstNote
                    repository.updateNote(it)
            }
        }
    }

    fun deleteUnused() {
        viewModelScope.launch(Dispatchers.IO) {
            note?.let { repository.deleteOneNote(it) }
        }
    }

    fun deleteNoteContent(noteContent: NoteContent){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNoteContent(noteContent)
        }
    }
}