package com.example.noteexample.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Flags
import com.example.noteexample.repository.NoteRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for [com.example.noteexample.ui.SettingsFragment]
 */
class SettingsViewModel @ViewModelInject internal constructor(
    val repository: NoteRepository
): ViewModel(){

    //Variables
    var flags: Flags? = null

    //LiveData
    val flagsLiveData: LiveData<Flags> = repository.flags

    //DB functions
    fun updateFlags(){
        viewModelScope.launch {
            flags?.let { repository.updateFlags(it) }
        }
    }
}