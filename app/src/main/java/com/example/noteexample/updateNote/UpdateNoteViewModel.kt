package com.example.noteexample.updateNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.*

class UpdateNoteViewModel(
    noteId: Int = 0,
    application: Application
) : AndroidViewModel(application) {

    //Repository
    private val repository: NoteRepository
    val currentNote: LiveData<Note>

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        currentNote = repository.selectNote(noteId)
    }

    private val _navigateToNoteFragment = MutableLiveData<Boolean>()
    val navigateToNoteFragment: LiveData<Boolean> = _navigateToNoteFragment

    fun onStartNavigating() {
        _navigateToNoteFragment.value = true
    }

    fun onStopNavigating() {
        _navigateToNoteFragment.value = false
    }

    fun onUpdate(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }
}