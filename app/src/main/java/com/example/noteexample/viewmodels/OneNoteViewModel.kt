package com.example.noteexample.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.adapters.NoteWithImagesRecyclerItems

/**
 * ViewModel for [com.example.noteexample.ui.OneNoteFragment]
 * and [com.example.noteexample.ui.OnePhotoFragment]
 */
class OneNoteViewModel(
    application: Application,
    noteID: Long,
) : AndroidViewModel(application) {

    //LiveData
    val currentNoteLiveData: LiveData<NoteWithImages>

    //Variables
    var scrollPosition: Int = 0
    val dataItemList = mutableListOf<NoteWithImagesRecyclerItems>()

    private val repository: NoteRepository
    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        currentNoteLiveData = repository.getNoteLiveData(noteID)
    }
}