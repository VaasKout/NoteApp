package com.example.noteexample.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.adapters.NoteWithImagesRecyclerItems
import com.example.noteexample.database.FirstNote
import kotlinx.coroutines.launch

/**
 * ViewModel for [com.example.noteexample.ui.OneNoteFragment]
 * and [com.example.noteexample.ui.OneNotePagerFragment]
 */
class OneNoteViewModel(
    noteID: Long,
    val repository: NoteRepository
) : ViewModel() {

    //LiveData
    val currentNoteLiveData: LiveData<NoteWithImages> = repository.getNoteLiveData(noteID)
    var currentNote: NoteWithImages? = null
        set(value) {
            field = value
            sortItems(field)
        }

    //Variables
    var animationOnEnd = false
    var scrollPosition: Int = 0
    var dataItemList = mutableListOf<NoteWithImagesRecyclerItems>()

    private fun sortItems(noteWithImages: NoteWithImages?) {
        noteWithImages?.let {
            it.notes = noteWithImages.notes.sortedBy { note -> note.notePos }
            it.images = noteWithImages.images.sortedBy { image -> image.imgPos }
        }
    }


    fun createNoteList() {
        dataItemList = mutableListOf()
        currentNote?.let { item ->

            dataItemList.add(0, NoteWithImagesRecyclerItems(header = item.header))
            val size = item.notes.size + item.images.size
            for (i in 0 until size) {
                item.notes.forEach { note ->
                    if (note.notePos == i) {
                        dataItemList.add(
                            NoteWithImagesRecyclerItems(
                                firstNote = note
                            )
                        )
                    }
                }
                item.images.forEach { image ->
                    if (image.imgPos == i) {
                        dataItemList.add(
                            NoteWithImagesRecyclerItems(
                                image = image
                            )
                        )
                    }
                }
            }
            Log.e("header", dataItemList.toString())
        }
    }

    fun updateFirstNote(firstNote: FirstNote?) {
        firstNote?.let {
            viewModelScope.launch {
                repository.updateFirstNote(firstNote)
            }
        }
    }
}