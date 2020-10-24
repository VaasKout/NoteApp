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

    /**
     * This list is needed to reflect changes in [UpdateNoteFragment]
     */
    var titleText = ""
    var startNoteContentList = mutableListOf<NoteContent>()
    var note: Note? = null

    //Repository
    private val repository: NoteRepository
    val currentNote: LiveData<Note>

    //LiveData
    val allNoteContent: LiveData<List<NoteContent>>
    private val _navigateToOneNoteFragment = MutableLiveData<Boolean>()
    val navigateToOneNoteFragment: LiveData<Boolean> = _navigateToOneNoteFragment

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        currentNote = repository.selectNote(noteID)
        allNoteContent = repository.allNoteContent
    }

    fun onStartNavigating() {
        _navigateToOneNoteFragment.value = true
    }

    fun onDoneNavigating() {
        _navigateToOneNoteFragment.value = false
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

    fun updateCurrentNote(title: String){
        viewModelScope.launch(Dispatchers.IO) {
            val note = currentNote.value
            note?.let {
                it.title = title
                repository.updateNote(it)
            }
        }
    }


    fun updateNoteContent(noteContent: List<NoteContent>){
        viewModelScope.launch (Dispatchers.IO){
            repository.updateNoteContentList(noteContent)
        }
    }

    fun deleteUnused() {
        viewModelScope.launch(Dispatchers.IO) {
            currentNote.value?.let { repository.deleteOneNote(it) }
        }
    }
}