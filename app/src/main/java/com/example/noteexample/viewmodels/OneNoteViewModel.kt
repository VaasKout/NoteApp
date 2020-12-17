package com.example.noteexample.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.adapters.NoteWithImagesRecyclerItems

/**
 * ViewModel for [com.example.noteexample.ui.OneNoteFragment]
 * and [com.example.noteexample.ui.OnePhotoFragment]
 */
class OneNoteViewModel(
    noteID: Long,
    repository: NoteRepository
) : ViewModel() {

    //LiveData
    val currentNoteLiveData: LiveData<NoteWithImages> = repository.getNoteLiveData(noteID)

    //Variables
    var animationOnEnd = false
    var scrollPosition: Int = 0
    val dataItemList = mutableListOf<NoteWithImagesRecyclerItems>()
}