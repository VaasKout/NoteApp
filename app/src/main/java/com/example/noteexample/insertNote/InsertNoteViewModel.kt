package com.example.noteexample.insertNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.*

class InsertNoteViewModel(application: Application) : AndroidViewModel(application) {

    //Flags
    var backPressed = false
    var allHidden = true
    var noteInserted = false

    //Repository
    private val repository: NoteRepository

    //Variables
    var title = ""
    var firstNote = ""
    var note: Note? = null
    var size = 0
    var noteContentList = listOf<NoteContent>()


    //Live Data
    private val _navigateToNoteFragment = MutableLiveData<Boolean>()
    val navigateToNoteFragment: LiveData<Boolean> = _navigateToNoteFragment
    val allNotes: LiveData<List<Note>>
    val allNoteContent: LiveData<List<NoteContent>>


    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
        allNotes = repository.allNotes
    }

    /**
     * functions for navigating
     */

    fun onStartNavigating() {
        _navigateToNoteFragment.value = true
    }

    fun onDoneNavigating() {
        _navigateToNoteFragment.value = false
        backPressed = false
    }

    /**
     * Coroutine methods
     */

    fun insertCameraPhoto(path: String) {
        viewModelScope.launch {
            note?.let {
                val localList = noteContentList.filter { item -> item.hidden }
                if (localList.isEmpty()) {
                    val noteContent = NoteContent(
                        noteId = it.id,
                        photoPath = path
                    )
                    repository.insertNoteContent(noteContent)
                } else {
                    localList[0].apply {
                        photoPath = path
                        hidden = false
                        repository.updateNoteContent(this)
                    }
                }
            }
        }
    }

    suspend fun insertNote() {
        if (!noteInserted) {
            note = Note()
            note?.let {
                repository.insertNote(it)
            }
            note = repository.getLastNote()
            noteInserted = true
        }
    }

    fun updateNoteContentList(noteContentList: List<NoteContent>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNoteContentList(noteContentList)
        }
    }


    fun updateCurrentNote(title: String = "", firstNote: String = "") {
        viewModelScope.launch {
            val cal = Calendar.getInstance().time
            val time = SimpleDateFormat("HH:mm EE dd MMM", Locale.getDefault()).format(cal)
            note?.let {
                it.title = title
                it.firstNote = firstNote
                it.pos = size
                it.date = time
                if (noteContentList.isNotEmpty() && noteContentList.any { item -> !item.hidden }) {
                    it.hasNoteContent = true
                }
                repository.updateNote(it)
            }
        }
    }

    fun deleteUnused() {
        viewModelScope.launch {
            note?.let { repository.deleteNote(it) }
        }
    }

//    fun deleteNoteContent(noteContent: NoteContent) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.deleteNoteContent(noteContent)
//        }
//    }

    //    fun updateNoteContent(noteContent: NoteContent) {
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.updateNoteContent(noteContent)
//        }
//    }
}