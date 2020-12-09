package com.example.noteexample.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class NoteViewModelFactory(
    private val noteID: Long = -1,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EditNoteViewModel::class.java) -> {
                EditNoteViewModel(noteID, application) as T
            }
            modelClass.isAssignableFrom(GalleryViewModel::class.java) -> {
                GalleryViewModel(noteID, application) as T
            }
            modelClass.isAssignableFrom(OneNoteViewModel::class.java) -> {
                OneNoteViewModel(noteID, application) as T
            }
            else -> throw IllegalArgumentException("Unknown class in UpdateNoteViewModelFactory")
        }
    }
}