package com.example.noteexample.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.noteexample.database.Flags
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.ui.ALL
import com.example.noteexample.ui.PHOTOS_ONLY
import com.example.noteexample.ui.TEXT_ONLY
import kotlinx.coroutines.*

class AllNotesViewModel(application: Application) : AndroidViewModel(application) {

    //Flags
    var startedMove = false
    var searchStarted = false

    //Variables
    var flags: Flags? = null
    var noteList = listOf<NoteWithImages>()

    //LiveData
    var flagsLiveData: LiveData<Flags>

    private val _searchModeFlag = MutableLiveData<Boolean>()
    var searchModeFlag: LiveData<Boolean> = _searchModeFlag

    private val _actionModeFlag = MutableLiveData<Boolean>()
    var actionModeFlag: LiveData<Boolean> = _actionModeFlag

    //define repository and flagsLiveData
    private val repository: NoteRepository

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        flagsLiveData = repository.flags
    }

    /**
     * Swap algorithm is used in [com.example.noteexample.ui.AllNotesFragment.helper]
     * Position in recyclerView is attached to
     * [com.example.noteexample.database.Header.pos] in ACS or DESC order
     */
    fun swap(from: Int, to: Int) {
        flags?.let {
            val fromItem = noteList[from]
            (noteList as MutableList<NoteWithImages>).remove(noteList[from])
            (noteList as MutableList<NoteWithImages>).add(to, fromItem)
            if (it.ascendingOrder) {
                noteList.forEachIndexed { index, item ->
                    item.header.pos = index
                }
            } else {
                var pos = noteList.size - 1
                noteList.forEach { item ->
                    item.header.pos = pos
                    pos--
                }
            }
        }
    }

    /**
     * Delete empty [com.example.noteexample.database.Header]
     * and empty [com.example.noteexample.database.Image] objects
     * [Dispatchers.Default] is used in case of heavy list computations
     */
    suspend fun deleteUnused() {
        withContext(Dispatchers.Default) {
            var updateNeeded = false
            for (item in noteList) {
                if (item.header.title.isEmpty() &&
                    item.header.text.isEmpty() &&
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

    /**
     * Two next methods sort [noteList] in [flagsLiveData] in ASC or DESC order
     * @see com.example.noteexample.database.Flags
     */

    suspend fun getASCNotes(filter: Int) {
        when (filter) {
            ALL -> {
                noteList = repository.allASCSortedNotes()
            }
            TEXT_ONLY -> {
                noteList = repository.allASCSortedNotes()
                    .filter { it.images.isEmpty() }
            }
            PHOTOS_ONLY -> {
                noteList =
                    repository.allASCSortedNotes()
                        .filter { it.images.isNotEmpty() }
            }
        }
    }

    suspend fun getDESCNotes(filter: Int) {
        when (filter) {
            ALL -> {
                noteList = repository.allDESCSortedNotes()
            }
            TEXT_ONLY -> {
                noteList =
                    repository.allDESCSortedNotes().filter { it.images.isEmpty() }
            }
            PHOTOS_ONLY -> {
                noteList =
                    repository.allDESCSortedNotes().filter { it.images.isNotEmpty() }
            }
        }
    }

    /**
     * Methods for database
     *
     * updateFlags method triggers [flagsLiveData]
     * @see com.example.noteexample.repository.NoteRepository
     */

    fun onDeleteSelected() {
        viewModelScope.launch {
            val noteListToDelete =
                noteList.filter { it.header.isChecked }
            repository.deleteNoteWithImagesList(noteListToDelete)
            flags?.let { repository.updateFlags(it) }
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


    /**
     * LiveData methods
     */

    fun onStartActionMode() {
        _actionModeFlag.value = true
    }

    fun onDoneActionMode() {
        _actionModeFlag.value = false
    }

    fun onStartSearch() {
        _searchModeFlag.value = true
    }

    fun onDoneSearch() {
        _searchModeFlag.value = false
    }
}