package com.example.noteexample.oneNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository

class OneNoteViewModel(
    application: Application,
    private val noteID: Int = -1,
    private val noteContentID: Int = -1
) : AndroidViewModel(application) {

    private val repository: NoteRepository
    val allNoteContent: LiveData<List<NoteContent>>
    var currentNote: Note? = null
    var currentNoteContent: NoteContent? = null

    var imgClicked = false

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
    }

    suspend fun getNote() {
        currentNote = repository.getNote(noteID)
        currentNoteContent = repository.getNoteContent(noteContentID)
    }
}