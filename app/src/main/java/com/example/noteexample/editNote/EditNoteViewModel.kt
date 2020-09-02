package com.example.noteexample.editNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.*

class EditNoteViewModel(application: Application) : AndroidViewModel(application){
    //Repository
    private val repository : NoteRepository
    //Coroutines
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    //Live Data
    private val _navigateToNoteFragment = MutableLiveData<Boolean>()
    val navigateToNoteFragment : LiveData<Boolean> = _navigateToNoteFragment

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
    }

    /**
     * functions for navigating
     */

    fun onStartNavigating(){
        _navigateToNoteFragment.value = true
    }
    fun onDoneNavigating(){
        _navigateToNoteFragment.value = false
    }

    /**
     * Coroutine Pattern
     */

    fun onInsert(note: Note) = uiScope.launch {
        insert(note)
    }

    private suspend fun insert(note: Note){
        withContext(Dispatchers.IO){
            repository.insert(note)
        }
    }

    /**
     * clear viewModelJob
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}