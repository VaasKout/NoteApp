package com.example.noteexample.gallery

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class GalleryViewModelFactory(
    private val noteID: Long,
    private val application: Application
): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)){
            return GalleryViewModel(noteID, application) as T
        }
        throw IllegalArgumentException("Unknown class in UpdateNoteViewModelFactory")
    }
}