package com.example.noteexample.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class EditNoteViewModelFactory(
    private val noteID: Long = -1,
    private val application: Application) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)){
            return EditNoteViewModel(noteID, application) as T
        }
        throw IllegalArgumentException("Unknown class in UpdateNoteViewModelFactory")
    }
}