package com.example.noteexample.insertNote

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Camera
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.*

class InsertNoteViewModel(application: Application) : AndroidViewModel(application) {

    //Flag
    var noteInserted = false

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

    fun onInsert(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

     fun getLastNote() {
        viewModelScope.launch(Dispatchers.IO) {
            _currentNote.postValue(repository.getLastNote())
        }
    }

    fun deleteUnused() {
        viewModelScope.launch(Dispatchers.IO) {
            _currentNote.value?.let { repository.deleteOneNote(it) }
        }
    }
}