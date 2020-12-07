package com.example.noteexample.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Flags
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for [com.example.noteexample.ui.SettingsFragment]
 */
class SettingsViewModel (application: Application): AndroidViewModel(application){

    //Variables
    var flags: Flags? = null

    //LiveData
    val flagsLiveData: LiveData<Flags>


    private val repository: NoteRepository
    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        flagsLiveData = repository.flags
    }

    //DB functions
    fun updateFlags(){
        viewModelScope.launch {
            flags?.let { repository.updateFlags(it) }
        }
    }
}