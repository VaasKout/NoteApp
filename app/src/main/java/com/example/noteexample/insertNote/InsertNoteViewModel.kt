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

    var noteID = -1
    //Flags
    private var noteInserted = false
    var backPressed = false

    //Repository
    private val repository: NoteRepository

    //Live Data
    private val _navigateToNoteFragment = MutableLiveData<Boolean>()
    val navigateToNoteFragment: LiveData<Boolean> = _navigateToNoteFragment

    val allNoteContent: LiveData<List<NoteContent>>

    private val _currentNote = MutableLiveData<Note>()
    val currentNote: LiveData<Note> = _currentNote


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

    fun updateNoteContent(noteContent: List<NoteContent>){
        viewModelScope.launch (Dispatchers.IO){
            repository.updateNoteContentList(noteContent)
        }
    }

    fun insertPhoto(path: String){
        viewModelScope.launch (Dispatchers.IO) {
            val noteContent = NoteContent(
                noteId = noteID,
                photoPath = path
            )
            repository.insertNoteContent(noteContent)
        }
    }

     fun updateCurrentNote(title: String = "", firstNote: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            if (!noteInserted){
                repository.insertNote(Note())
                noteInserted = true
            }
            val note = repository.getLastNote()
            if (title.isNotEmpty() || firstNote.isNotEmpty()){
                note.title = title
                note.firstNote = firstNote
                repository.updateNote(note)
            }
            _currentNote.postValue(note)
        }
    }

    fun deleteUnused() {
        viewModelScope.launch(Dispatchers.IO) {
            _currentNote.value?.let { repository.deleteOneNote(it) }
        }
    }
}