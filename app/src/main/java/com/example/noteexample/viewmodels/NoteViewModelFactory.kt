package com.example.noteexample.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.noteexample.repository.NoteRepository
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class NoteViewModelFactory(
    private val noteID: Long = -1,
    private val repository: NoteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(EditNoteViewModel::class.java) -> {
                EditNoteViewModel(noteID, repository) as T
            }
            modelClass.isAssignableFrom(GalleryViewModel::class.java) -> {
                GalleryViewModel(noteID, repository) as T
            }
            modelClass.isAssignableFrom(OneNoteViewModel::class.java) -> {
                OneNoteViewModel(noteID, repository) as T
            }
            else -> throw IllegalArgumentException("Unknown class in UpdateNoteViewModelFactory")
        }
    }
}