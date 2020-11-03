package com.example.noteexample.updateNote

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class UpdateNoteViewModelFactory(
    private val noteId: Int = 0,
    private val application: Application) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateNoteViewModel::class.java)){
            return UpdateNoteViewModel(noteId, application) as T
        }
        throw IllegalArgumentException("Unknown class in UpdateNoteViewModelFactory")
    }
}