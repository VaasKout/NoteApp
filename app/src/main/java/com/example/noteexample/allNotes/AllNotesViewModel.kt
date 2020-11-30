package com.example.noteexample.allNotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.Flags
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.settings.ALL
import com.example.noteexample.settings.PHOTOS_ONLY
import com.example.noteexample.settings.TEXT_ONLY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllNotesViewModel(application: Application) : AndroidViewModel(application) {

    //Flags
    var startedMove = false
    var searchStarted = false

    //Variables
    var flags: Flags? = null
    var noteList = mutableListOf<NoteWithImages>()

    //LiveData
    var flagsLiveData: LiveData<Flags>

    private val _searchModeFlag = MutableLiveData<Boolean>()
    var searchModeFlag: LiveData<Boolean> = _searchModeFlag

    private val _actionModeFlag = MutableLiveData<Boolean>()
    var actionModeFlag: LiveData<Boolean> = _actionModeFlag

    private val repository: NoteRepository

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        flagsLiveData = repository.flags
    }

    /**
     * Coroutine functions
     */

    suspend fun getASCNotes(filter: Int) {
        noteList = mutableListOf()
        when (filter) {
            ALL -> {
                noteList.addAll(repository.allASCSortedNotes())
            }
            TEXT_ONLY -> {
                noteList.addAll(repository.allASCSortedNotes()
                    .filter { !it.note.hasNoteContent })
            }
            PHOTOS_ONLY -> {
                noteList.addAll(
                    repository.allASCSortedNotes()
                        .filter { it.note.hasNoteContent })
            }
        }
    }

    suspend fun getDESCNotes(filter: Int) {
        noteList = mutableListOf()
        when (filter) {
            ALL -> {
                noteList.addAll(repository.allDESCSortedNotes())
            }
            TEXT_ONLY -> {
                noteList.addAll(
                    repository.allDESCSortedNotes().filter { !it.note.hasNoteContent })
            }
            PHOTOS_ONLY -> {
                noteList.addAll(
                    repository.allDESCSortedNotes().filter { it.note.hasNoteContent })
            }
        }
    }

    fun onDeleteSelected() {
        viewModelScope.launch {
            val noteListToDelete =
                noteList.filter { it.note.isChecked }
            repository.deleteNoteWithImagesList(noteListToDelete)
            flags?.let { repository.updateFlags(it) }
        }

    }

    suspend fun deleteUnused() {
        withContext(Dispatchers.Default) {
            var updateNeeded = false
            for (item in noteList) {
                if (item.note.title.isEmpty() &&
                    item.note.text.isEmpty() &&
                    (item.images.isEmpty() ||
                            item.images.none { !it.hidden || it.signature.isNotEmpty() })
                ) {
                    updateNeeded = true
                    repository.deleteNoteWithImages(item)
                    continue
                }
                for (image in item.images) {
                    if (image.hidden) {
                        if (image.signature.isNotEmpty()) {
                            image.photoPath = ""
                            image.hidden = false
                            repository.updateImage(image)
                        } else {
                            repository.deleteImage(image)
                        }
                        updateNeeded = true
                    }
                }
            }
            if (updateNeeded) {
                flags?.let { repository.updateFlags(it) }
            }
        }
    }

    fun updateNoteList() {
        viewModelScope.launch {
            repository.updateNoteWithImagesList(noteList)
            flags?.let { repository.updateFlags(it) }
        }
    }

    fun onClear() {
        viewModelScope.launch {
            repository.deleteAllNotesWithImages()
            flags?.let { repository.updateFlags(it) }
        }
    }

    fun swap(from: Int, to: Int) {
        flags?.let {
            val fromItem = noteList[from]
            noteList.remove(noteList[from])
            noteList.add(to, fromItem)
            if (it.ascendingOrder) {
                noteList.forEachIndexed { index, item ->
                    item.note.pos = index
                }
            } else {
                var pos = noteList.size - 1
                noteList.forEach { item ->
                    item.note.pos = pos
                    pos--
                }
            }
        }
    }

    fun onStartActionMode(){
        _actionModeFlag.value = true
    }

    fun onDoneActionMode(){
        _actionModeFlag.value = false
    }

    fun onStartSearch() {
        _searchModeFlag.value = true
    }

    fun onDoneSearch() {
        _searchModeFlag.value = false
    }
}