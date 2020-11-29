package com.example.noteexample.oneNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.NoteWithImagesRecyclerItems

class OneNoteViewModel(
    application: Application,
    noteID: Long,
) : AndroidViewModel(application) {

    private val repository: NoteRepository
    var scrollPosition: Int = 0

    val currentNoteLiveData: LiveData<NoteWithImages>
    val dataItemList = mutableListOf<NoteWithImagesRecyclerItems>()

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        currentNoteLiveData = repository.getNoteLiveData(noteID)
    }
}