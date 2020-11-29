package com.example.noteexample.allNotes

import android.annotation.SuppressLint
import android.app.Application
import android.view.ActionMode
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
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
    var flagsObj: Flags? = null
    var noteList = mutableListOf<NoteWithImages>()

    //LiveData
    var flags: LiveData<Flags>

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

    suspend fun onDeleteSelected() {
        val noteListToDelete =
            noteList.filter { it.note.isChecked }
        withContext(Dispatchers.IO) {
            repository.deleteNoteWithImagesList(noteListToDelete)
            flagsObj?.let { repository.updateFlags(it) }
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
            if (updateNeeded){
                withContext(Dispatchers.IO){
                    flagsObj?.let { repository.updateFlags(it) }
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

    fun onStopActionMode() {
        _actionMode.value?.finish()

    }

    @SuppressLint("NullSafeMutableLiveData")
    fun onDestroyActionMode() {
        _actionMode.value = null
    }
}