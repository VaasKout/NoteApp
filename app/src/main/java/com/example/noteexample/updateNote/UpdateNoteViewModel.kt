package com.example.noteexample.updateNote

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.*

class UpdateNoteViewModel(
    private val noteID: Int = 0,
    application: Application
) : AndroidViewModel(application) {

    //Flags
    var backPressed = false
    var textChanged = false
    var sizeChanged = false
    var startListInit = false

    /**
     * This list is needed to reflect changes in [UpdateNoteFragment]
     */

    var title = ""
    var firstNote = ""
    var newTitle = ""
    var newFirstNote = ""
    var startNoteContentList = mutableListOf<NoteContent>()
    var noteContentList = listOf<NoteContent>()

    //Repository
    private val repository: NoteRepository
    var currentNote: Note? = null

    //LiveData
    val allNoteContent: LiveData<List<NoteContent>>
    private val _navigateToOneNoteFragment = MutableLiveData<Boolean>()
    val navigateToOneNoteFragment: LiveData<Boolean> = _navigateToOneNoteFragment

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
        getNote()
    }

    fun onStartNavigating() {
        _navigateToOneNoteFragment.value = true
    }

    fun onDoneNavigating() {
        _navigateToOneNoteFragment.value = false
    }

    //Dao functions
    private fun getNote() {
        viewModelScope.launch {
            currentNote = repository.getNote(noteID)
            currentNote?.let {
                title = it.title
                firstNote = it.firstNote
                newTitle = it.title
                newFirstNote = it.firstNote
            }
        }
    }

    fun insertCameraPhoto(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            currentNote?.let {
                val localList = noteContentList.filter { list -> list.hidden }
                if (localList.isEmpty()) {
                    val noteContent = NoteContent(
                        noteId = it.id,
                        photoPath = path
                    )
                    repository.insertNoteContent(noteContent)
                } else {
                    localList.forEach { item ->
                        if (item.hidden) {
                            item.photoPath = path
                            item.hidden = false
                            Log.e("it.note", item.note)
                            repository.updateNoteContent(item)
                            return@forEach
                        }
                    }
                }
            }
        }
    }

    fun insertNoteContentList(noteContent: List<NoteContent>) {
        viewModelScope.launch {
            repository.insertNoteContentList(noteContent)
        }
    }

    fun updateCurrentNote(title: String, firstNote: String) {
        viewModelScope.launch(Dispatchers.IO) {
            currentNote?.let {
                it.title = title
                it.firstNote = firstNote
                repository.updateNote(it)
            }
        }
    }

    fun updateHidden() {
        viewModelScope.launch(Dispatchers.IO) {
            noteContentList.forEach {
                if (it.hidden) {
                    if (it.note.isNotEmpty()) {
                        it.photoPath = ""
                        repository.updateNoteContent(it)
                    } else {
                        repository.deleteNoteContent(it)
                    }
                }
            }
        }
    }

    fun updateNoteContentList(noteContent: List<NoteContent>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNoteContentList(noteContent)
        }
    }


    fun deleteUnused() {
        viewModelScope.launch {
            currentNote?.let { repository.deleteNote(it) }
        }
    }


    fun deleteNoteContentList(noteContent: List<NoteContent>) {
        viewModelScope.launch {
            repository.deleteNoteContentList(noteContent)
        }
    }

    //    fun deleteNoteContent(noteContent: NoteContent) {
//        viewModelScope.launch {
//            repository.deleteNoteContent(noteContent)
//        }
//    }

    //    fun updateNoteContent(noteContent: NoteContent){
//        viewModelScope.launch {
//            repository.updateNoteContent(noteContent)
//        }
//    }
}