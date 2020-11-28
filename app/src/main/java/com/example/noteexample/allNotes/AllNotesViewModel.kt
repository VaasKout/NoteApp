package com.example.noteexample.allNotes

import android.annotation.SuppressLint
import android.app.Application
import android.view.ActionMode
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.noteexample.database.*
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.settings.ALL
import com.example.noteexample.settings.PHOTOS_ONLY
import com.example.noteexample.settings.TEXT_ONLY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllNotesViewModel(application: Application) : AndroidViewModel(application) {

    //Flags
    var actionModeStarted = false
    var startedMove = false
    var searchStarted = false

    //Variables
    var scrollPosition: Int? = null
    var flagsObj: Flags? = null
    var noteList = mutableListOf<NoteWithImages>()

    //LiveData
    var flags: LiveData<Flags>
    private val _allSortedNotes = MutableLiveData<List<NoteWithImages>>()
    val allSortedNotes: LiveData<List<NoteWithImages>> = _allSortedNotes

    private val _searchMode = MutableLiveData<Boolean>()
    var searchMode: LiveData<Boolean> = _searchMode

    private val _actionMode = MutableLiveData<ActionMode>()
    var actionMode: LiveData<ActionMode> = _actionMode

    private val repository: NoteRepository

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        flags = repository.flags
    }

    /**
     * Coroutine functions
     */

    fun getASCNotes(filter: Int) {
        viewModelScope.launch {
            when (filter) {
                ALL -> {
                    _allSortedNotes.value = repository.allASCSortedNotes()
                }
                TEXT_ONLY -> {
                    _allSortedNotes.value =
                        repository.allASCSortedNotes().filter { !it.note.hasNoteContent }
                }
                PHOTOS_ONLY -> {
                    _allSortedNotes.value =
                        repository.allASCSortedNotes().filter { it.note.hasNoteContent }
                }
            }
        }
    }

    fun getDESCNotes(filter: Int) {
        viewModelScope.launch {
            when (filter) {
                ALL -> {
                    _allSortedNotes.value = repository.allDESCSortedNotes()
                }
                TEXT_ONLY -> {
                    _allSortedNotes.value =
                        repository.allDESCSortedNotes().filter { !it.note.hasNoteContent }
                }
                PHOTOS_ONLY -> {
                    _allSortedNotes.value =
                        repository.allDESCSortedNotes().filter { it.note.hasNoteContent }
                }
            }
        }
    }

    suspend fun onDeleteSelected() {
        val noteListToDelete =
            noteList.filter { it.note.isChecked }
        withContext(Dispatchers.IO) {
            repository.deleteNoteWithImagesList(noteListToDelete)
            flagsObj?.let { repository.updateFlags(it) }
        }
    }

    suspend fun deleteUnused() {
        for (item in noteList) {
            if (item.note.title.isEmpty() &&
                item.note.text.isEmpty() &&
                (item.images.isEmpty() ||
                        item.images.none { !it.hidden || it.signature.isNotEmpty() })
            ) {
                repository.deleteNoteWithImages(item)
                flagsObj?.let { repository.updateFlags(it) }

                continue
            }
            for (image in item.images) {
                if (image.hidden) {
                    if (image.signature.isNotEmpty()) {
                        image.photoPath = ""
                        image.hidden = false
                        repository.updateNoteWithImages(item)
                    } else {
                        repository.deleteImage(image)
                    }
                }
            }
        }
    }

    fun updateNoteList() {
        viewModelScope.launch {
            repository.updateNoteWithImagesList(noteList)
            flagsObj?.let { repository.updateFlags(it) }
        }
    }

    fun onClear() {
        viewModelScope.launch {
            repository.deleteAllNotesWithImages()
            flagsObj?.let { repository.updateFlags(it) }
        }
    }

    fun swap(from: Int, to: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val fromItem = noteList[from]
            if (fromItem.note.pos == from && noteList[to].note.pos == to) {
                noteList.remove(noteList[from])
                noteList.add(to, fromItem)
                noteList.forEachIndexed { index, item ->
                    item.note.pos = index
                }
            } else {
                noteList.remove(noteList[from])
                noteList.add(to, fromItem)
                var pos = noteList.size - 1
                noteList.forEach {
                    it.note.pos = pos
                    pos--
                }
            }
        }
    }

    //CallBacks

    fun onStartSearch() {
        _searchMode.value = true
    }

    fun onDoneSearch() {
        _searchMode.value = false
    }


    /**
     * Action mode lifecycle functions
     */

    fun onStartActionMode(activity: FragmentActivity, actionModeController: ActionMode.Callback) {
        _actionMode.value = activity.startActionMode(actionModeController)
        _actionMode.value?.title =
            "${noteList.filter { it.note.isChecked }.size}"
        actionModeStarted = true
    }

    fun onResumeActionMode() {
        _actionMode.value?.title =
            "${noteList.filter { it.note.isChecked }.size}"
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun onDestroyActionMode() {
        _actionMode.value?.finish()
        _actionMode.value = null
    }
}