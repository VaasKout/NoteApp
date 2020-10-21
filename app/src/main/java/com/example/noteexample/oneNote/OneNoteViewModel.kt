package com.example.noteexample.oneNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.launch

class OneNoteViewModel(
    application: Application,
    noteID: Int = 0): AndroidViewModel(application){

    private val repository: NoteRepository
    val allNoteContent: LiveData<List<NoteContent>>
    val currentNote: LiveData<Note>

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
        currentNote = repository.selectNote(noteID)
    }
}