package com.example.noteexample.editNote

import android.app.Application
import androidx.lifecycle.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.database.*
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.utils.NoteWithImagesRecyclerItems
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class EditNoteViewModel(
    private val noteID: Long = -1,
    application: Application
) : AndroidViewModel(application) {

    //Flags
    var allHidden = true
    var backPressed = false
    var itemListSame = false

    //Variables
    var position = 0
    var title = ""
    var text = ""
    var startNote: NoteWithImages? = null
    var currentNote: NoteWithImages? = null
    var dataItemList = mutableListOf<NoteWithImagesRecyclerItems>()

    //Repository
    private val repository: NoteRepository

    //LiveData
    val currentNoteLiveData: LiveData<NoteWithImages>

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    init {
        val noteDao = NoteRoomDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        currentNoteLiveData = liveData {
            emitSource(getNote())
        }
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
            viewModelScope.launch {
                currentNote?.let { repository.updateNoteWithImages(it) }
            }
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
        if (!itemListSame) {
            itemListSame = true
        }
        currentNote?.let {
            val tmpID = it.images[from].imgID
            it.images[from].imgID = it.images[to].imgID
            it.images[to].imgID = tmpID
        }
    }

    //Dao functions
    private suspend fun getNote(): LiveData<NoteWithImages> {
        return if (noteID > -1) {
            startNote = repository.getNote(noteID)
            repository.getNoteLiveData(noteID)
        } else {
            position = repository.allASCSortedNotes().size
            val note = Header(pos = position)
            repository.insertNote(note)
            startNote = repository.getLastNote()
            repository.getLastLiveData()
        }
    }


    fun updateCurrentNote() {
        viewModelScope.launch {
            if (noteID > -1) {
                currentNote?.let {
                    it.header.title = title
                    it.header.text = text
                    it.header.hasNoteContent =
                        it.images.isNotEmpty() && it.images.any { item -> !item.hidden }
                    repository.updateNoteWithImages(it)
                }
            } else {
                val cal = Calendar.getInstance().time
                val time =
                    SimpleDateFormat("HH:mm EE dd MMM", Locale.getDefault()).format(cal)
                currentNote?.let {
                    it.header.title = title
                    it.header.text = text
                    it.header.date = time
                    it.header.hasNoteContent =
                        it.images.isNotEmpty() && it.images.any { item -> !item.hidden }
                    repository.updateNoteWithImages(it)
                }
            }
        }
    }

    fun onStartNavigating() {
        _navigateBack.value = true
    }

    fun onDoneNavigating() {
        _navigateBack.value = false
    }


    fun insertCameraPhoto(path: String) {
        viewModelScope.launch {
            currentNote?.let {
                val localList = it.images.filter { list -> list.hidden }
                if (localList.isEmpty()) {
                    val image = Image(
                        parentNoteID = it.header.noteID,
                        photoPath = path
                    )
                    repository.insertImage(image)
                } else {
                    localList[0].apply {
                        photoPath = path
                        hidden = false
                        repository.updateImage(this)
                    }
                }
            }
        }
    }

    fun insertNoteWithImages(noteWithImages: NoteWithImages?) {
        noteWithImages?.let {
            viewModelScope.launch {
                repository.insertNoteWithImages(noteWithImages)
            }
        }
    }

    fun deleteNote(noteWithImages: NoteWithImages?) {
        noteWithImages?.let {
            viewModelScope.launch {
                repository.deleteNoteWithImages(it)
            }
        }
    }

    fun deleteImage(image: Image) {
        viewModelScope.launch {
            repository.deleteImage(image)
        }
    }

    fun deleteUnused() {
        viewModelScope.launch {
            currentNote?.let { repository.deleteNoteWithImages(it) }
        }
    }
}