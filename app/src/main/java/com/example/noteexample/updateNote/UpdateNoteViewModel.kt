package com.example.noteexample.updateNote

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
    val noteContentList = mutableListOf<NoteContent>()

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
    private fun getNote(){
        viewModelScope.launch(Dispatchers.IO) {
            while (currentNote == null){
                currentNote = repository.getNote(noteID)
            }
            currentNote?.let {
                title = it.title
                firstNote = it.firstNote
                newTitle = it.title
                newFirstNote = it.firstNote
            }
        }
    }

    fun insertPhoto(path: String){
        viewModelScope.launch (Dispatchers.IO) {
            val noteContent = NoteContent(
                noteId = noteID,
                photoPath = path
            )
            repository.insertNoteContent(noteContent)
        }
    }

    fun insertNoteContent(noteContent: List<NoteContent>){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNoteContentList(noteContent)
        }
    }

    fun updateCurrentNote(title: String, firstNote: String){
        viewModelScope.launch(Dispatchers.IO) {
            currentNote?.let {
                it.title = title
                it.firstNote = firstNote
                repository.updateNote(it)
            }
        }
    }


    fun updateNoteContentList(noteContent: List<NoteContent>){
        viewModelScope.launch (Dispatchers.IO){
            repository.updateNoteContentList(noteContent)
        }
    }

    fun updateNoteContent(noteContent: NoteContent){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNoteContent(noteContent)
        }
    }

    fun deleteUnused() {
        viewModelScope.launch(Dispatchers.IO) {
            currentNote?.let { repository.deleteOneNote(it) }
        }
    }

    fun deleteNoteContent(noteContent: NoteContent){
        viewModelScope.launch(Dispatchers.IO) {
           repository.deleteNoteContent(noteContent)
        }
    }

    fun deleteNoteContentList(noteContent: List<NoteContent>){
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNoteContentList(noteContentList)
        }
    }
}