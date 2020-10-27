package com.example.noteexample.oneNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OneNoteViewModel(
    application: Application,
    private val noteID: Int = 0): AndroidViewModel(application){
    var viewModelInit = false

    private val repository: NoteRepository
    val allNoteContent: LiveData<List<NoteContent>>
    var currentNote: Note? = null

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
        if (!viewModelInit){
            getNote()
            viewModelInit = true
        }

    }

    fun getNote(){
        viewModelScope.launch(Dispatchers.IO) {
            currentNote = repository.getNote(noteID)
        }
    }
}