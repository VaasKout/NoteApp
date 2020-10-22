package com.example.noteexample.insertNote

import android.app.Application
import androidx.lifecycle.*
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.*

class InsertNoteViewModel(application: Application) : AndroidViewModel(application) {

    //Flags
    private var noteInserted = false
    var noteContentIsEmpty = true
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

     fun updateCurrentNote(text: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            if (!noteInserted){
                repository.insertNote(Note())
                noteInserted = true
            }
            val note = repository.getLastNote()
            if (text.isNotEmpty()){
                note.title = text
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