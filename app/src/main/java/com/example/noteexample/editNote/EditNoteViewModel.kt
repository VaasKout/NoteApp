package com.example.noteexample.editNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteRoomDatabase
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.DataItem
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class EditNoteViewModel(
    private val noteID: Int = -1,
    application: Application
) : AndroidViewModel(application) {

    //Flags
    var allHidden = true
    private var noteInserted = false
    var backPressed = false
    var textChanged = false
    var sizeChanged = false
    var startListInit = false
    var itemListInit = false
    private var noteInit = false

    //Variables
    var currentNote: Note? = null
    var lastIndex = 0
    var title = ""
    var firstNote = ""
    var newTitle = ""
    var newFirstNote = ""
    var startNoteContentList = mutableListOf<NoteContent>()
    var noteContentList = mutableListOf<NoteContent>()
    var dataItemList = mutableListOf<DataItem>()

    //Repository
    private val repository: NoteRepository

    //LiveData
    val allNotes: LiveData<List<Note>>
    val allNoteContent: LiveData<List<NoteContent>>
    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNoteContent = repository.allNoteContent
        allNotes = repository.allNotes
    }

    val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or
                ItemTouchHelper.DOWN or ItemTouchHelper.UP,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.adapterPosition - 1
            val to = target.adapterPosition - 1

            if (from >= 0 && to >= 0) {
                swap(from, to)
                recyclerView.adapter?.notifyItemMoved(from + 1, to + 1)
            }
            return true
        }

        override fun clearView(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ) {
            super.clearView(recyclerView, viewHolder)
            updateNoteContentList(noteContentList)
        }

        override fun onSwiped(
            viewHolder: RecyclerView.ViewHolder,
            direction: Int
        ) {
        }

    })

    /**
     * Coroutine methods
     */

    fun swap(from: Int, to: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val tmpID = noteContentList[from].id
            noteContentList[from].id = noteContentList[to].id
            noteContentList[to].id = tmpID
        }
    }

    suspend fun insertNote() {
        if (!noteInserted) {
            currentNote = Note()
            currentNote?.let {
                repository.insertNote(it)
            }
            currentNote = repository.getLastNote()
            noteInserted = true
        }
    }


    fun updateCurrentNoteInsertFr(title: String = "", firstNote: String = "") {
        viewModelScope.launch {
            val cal = Calendar.getInstance().time
            val time = SimpleDateFormat("HH:mm EE dd MMM", Locale.getDefault()).format(cal)
            currentNote?.let {
                it.title = title
                it.firstNote = firstNote
                it.pos = lastIndex
                it.date = time
                it.hasNoteContent =
                    noteContentList.isNotEmpty() && noteContentList.any { item -> !item.hidden }
                repository.updateNote(it)
            }
        }
    }

    fun updateCurrentNoteUpdateFr(title: String, firstNote: String) {
        viewModelScope.launch {
            currentNote?.let {
                it.title = title
                it.firstNote = firstNote
                it.hasNoteContent =
                    noteContentList.isNotEmpty() && noteContentList.any { item -> !item.hidden }
                repository.updateNote(it)
            }
        }
    }


    fun onStartNavigating() {
        _navigateBack.value = true
    }

    fun onDoneNavigating() {
        _navigateBack.value = false
    }

    //Dao functions
    suspend fun getNote() {
        if (!noteInit) {
            currentNote = repository.getNote(noteID)
            currentNote?.let {
                title = it.title
                firstNote = it.firstNote
                newTitle = it.title
                newFirstNote = it.firstNote
            }
            noteInit = true
        }
    }

    fun insertCameraPhoto(path: String) {
        viewModelScope.launch {
            currentNote?.let {
                val localList = noteContentList.filter { list -> list.hidden }
                if (localList.isEmpty()) {
                    val noteContent = NoteContent(
                        noteId = it.id,
                        photoPath = path
                    )
                    repository.insertNoteContent(noteContent)
                } else {
                    localList[0].apply {
                        photoPath = path
                        hidden = false
                        repository.updateNoteContent(this)
                    }
                }
            }
        }
    }

    fun insertNoteContentList(noteContent: List<NoteContent>) {
        viewModelScope.launch {
            repository.insertNoteContentList(noteContent)
        }
    }


    fun updateNoteContentList(noteContent: List<NoteContent>) {
        viewModelScope.launch {
            repository.updateNoteContentList(noteContent)
        }
    }


    fun deleteUnused() {
        viewModelScope.launch {
            currentNote?.let { repository.deleteNote(it) }
        }
    }


    fun deleteNoteContentList(noteContent: List<NoteContent>) {
        viewModelScope.launch {
            repository.deleteNoteContentList(noteContent)
        }
    }

    fun deleteNoteContent(noteContent: NoteContent) {
        viewModelScope.launch {
            repository.deleteNoteContent(noteContent)
        }
    }

    //    fun updateNoteContent(noteContent: NoteContent){
//        viewModelScope.launch {
//            repository.updateNoteContent(noteContent)
//        }
//    }
}