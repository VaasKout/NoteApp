package com.example.noteexample.oneNote

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class OneNoteViewModelFactory (
    private val application: Application,
    private val noteID: Int = 0
): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OneNoteViewModel::class.java)){
            return OneNoteViewModel(application, noteID) as T
        }
        throw IllegalArgumentException("Unknown class in OneNoteViewModelFactory")
    }
}