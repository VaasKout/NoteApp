package com.example.noteexample.updateNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.*

class UpdateNoteViewModel(noteId: Int = 0,
                          application: Application) : AndroidViewModel(application){

    //Repository
    private val repository : NoteRepository
    val currentNote : LiveData<Note>

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        currentNote = repository.selectNote(noteId)
    }

    private val _navigateToNoteFragment = MutableLiveData<Boolean>()
    val navigateToNoteFragment : LiveData<Boolean> = _navigateToNoteFragment

    fun onStartNavigating(){
        _navigateToNoteFragment.value = true
    }
    fun onStopNavigating(){
        _navigateToNoteFragment.value = false
    }


    private val viewModelJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private suspend fun update(note: Note){
        withContext(Dispatchers.IO){
            repository.updateNote(note)
        }
    }
    fun onUpdate(note: Note){
        scope.launch {
            update(note)
        }
    }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}