package com.example.noteexample.editNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class EditNoteViewModel(
    private val noteID: Int = -1,
    application: Application
) : AndroidViewModel(application) {

    //Flags
    var allHidden = true
    private var noteInserted = false
    var backPressed = false
    var textChanged = false
    var sizeChanged = false
    var startListInit = false
    private var noteInit = false

    //Variables
    var currentNote: Note? = null
    var size = 0
    var title = ""
    var firstNote = ""
    var newTitle = ""
    var newFirstNote = ""
    var startNoteContentList = mutableListOf<NoteContent>()
    var noteContentList = listOf<NoteContent>()


    //Live Data
    val allNotes: LiveData<List<Note>>

    /**
     * This list is needed to reflect changes in [UpdateNoteFragment]
     */

    //Repository
    private val repository: NoteRepository


    //LiveData
    val allNoteContent: LiveData<List<NoteContent>>
    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
        allNotes = repository.allNotes
    }

    /**
     * Coroutine methods
     */

    suspend fun insertNote() {
        if (!noteInserted) {
            currentNote = Note()
            currentNote?.let {
                repository.insertNote(it)
            }
            currentNote = repository.getLastNote()
            noteInserted = true
        }
    }


    fun updateCurrentNoteInsertFr(title: String = "", firstNote: String = "") {
        viewModelScope.launch {
            val cal = Calendar.getInstance().time
            val time = SimpleDateFormat("HH:mm EE dd MMM", Locale.getDefault()).format(cal)
            currentNote?.let {
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


    fun onStartNavigating() {
        _navigateBack.value = true
    }

    fun onDoneNavigating() {
        _navigateBack.value = false
    }

    //Dao functions
   suspend fun getNote() {
        if (!noteInit){
            currentNote = repository.getNote(noteID)
            currentNote?.let {
                title = it.title
                firstNote = it.firstNote
                newTitle = it.title
                newFirstNote = it.firstNote
            }
            noteInit = true
        }
    }

    fun insertCameraPhoto(path: String) {
        viewModelScope.launch {
            currentNote?.let {
                val localList = noteContentList.filter { list -> list.hidden }
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

    fun insertNoteContentList(noteContent: List<NoteContent>) {
        viewModelScope.launch {
            repository.insertNoteContentList(noteContent)
        }
    }

    fun updateCurrentNoteUpdateFr(title: String, firstNote: String) {
        viewModelScope.launch {
            currentNote?.let {
                it.title = title
                it.firstNote = firstNote
                if (noteContentList.isNotEmpty() && noteContentList.any { item -> !item.hidden }) {
                    it.hasNoteContent = true
                }
                repository.updateNote(it)
            }
        }
    }

    fun updateNoteContentList(noteContent: List<NoteContent>) {
        viewModelScope.launch {
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

        fun deleteNoteContent(noteContent: NoteContent) {
        viewModelScope.launch {
            repository.deleteNoteContent(noteContent)
        }
    }

    //    fun updateNoteContent(noteContent: NoteContent){
//        viewModelScope.launch {
//            repository.updateNoteContent(noteContent)
//        }
//    }
}