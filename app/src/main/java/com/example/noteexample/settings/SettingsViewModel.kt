package com.example.noteexample.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Flags
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.launch

class SettingsViewModel (application: Application): AndroidViewModel(application){

    private val repository: NoteRepository
    val flags: LiveData<Flags>
    var flagsObj: Flags? = null

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        flags = repository.flags
    }

    fun updateFlags(){
        viewModelScope.launch {
            flagsObj?.let { repository.updateFlags(it) }
        }
    }
}