package com.example.noteexample.viewmodels

import androidx.lifecycle.*
import com.example.noteexample.database.Header
import com.example.noteexample.database.Image
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.adapters.NoteWithImagesRecyclerItems
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for [com.example.noteexample.ui.EditNoteFragment]
 */
class EditNoteViewModel(
    private val noteID: Long = -1,
    private val repository: NoteRepository
) : ViewModel() {

    /**
     * [allHidden] checks if every img is hidden and doesn't have signature
     * [backPressed] checks if user pressed backButton
     * [itemListSame] prevents full [noteList] update to prevent submit another list,
     * which cause unacceptable animation
     */
    //Flags
    var allHidden = true
    var backPressed = false
    var itemListSame = false

    //Variables
    var position = 0
    var startNote: NoteWithImages? = null
    var currentNote: NoteWithImages? = null
    var noteList = mutableListOf<NoteWithImagesRecyclerItems>()

    //LiveData
    val currentNoteLiveData: LiveData<NoteWithImages>

    private val _navigateBack = MutableLiveData<Boolean>()
    val navigateBack: LiveData<Boolean> = _navigateBack

    init {
        /**
         * this LiveData get it's value asynchronously,
         * for new note it inserts new Header object in DB,
         * for existing note it gets value from DB
         */
        currentNoteLiveData = liveData {
            emitSource(getNote())
        }
    }

    /**
     * [swap] is attached to [com.example.noteexample.ui.EditNoteFragment.helper]
     * to sort images manually, this function swaps imgIDs
     */
    fun swap(from: Int, to: Int) {
        itemListSame = true
        currentNote?.let {
            val tmpID = it.images[from].imgID
            it.images[from].imgID = it.images[to].imgID
            it.images[to].imgID = tmpID
        }
    }

    /**
     * Insert note in [com.example.noteexample.ui.EditNoteFragment.startCamera]
     */
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

    /**
     * Emit source for [currentNoteLiveData]
     */
    private suspend fun getNote(): LiveData<NoteWithImages> {
        return if (noteID > -1) {
            startNote = repository.getNote(noteID)
            repository.getNoteLiveData(noteID)
        } else {
            position = repository.allASCSortedNotes().size
            val header = Header(pos = position)
            repository.insertNote(header)
            startNote = repository.getLastNote()
            repository.getLastLiveData()
        }
    }

    /**
     * Database methods
     */

    fun updateCurrentNote() {
        viewModelScope.launch {
            if (noteID > -1) {
                currentNote?.let {
                    repository.updateNoteWithImages(it)
                }
            } else {
                /**
                 * Each note has it's date
                 * @see com.example.noteexample.database.Header.date
                 */
                val cal = Calendar.getInstance().time
                val time =
                    SimpleDateFormat("HH:mm EE dd MMM", Locale.getDefault()).format(cal)
                currentNote?.let {
                    it.header.date = time
                    repository.updateNoteWithImages(it)
                }
            }
        }
    }

    suspend fun insertNoteWithImages(noteWithImages: NoteWithImages?) {
        noteWithImages?.let {
            repository.insertNoteWithImages(noteWithImages)
        }
    }

    suspend fun deleteNoteWithImages(noteWithImages: NoteWithImages?) {
        noteWithImages?.let {
            repository.deleteNoteWithImages(it)
        }
    }

    fun deleteImage(image: Image) {
        viewModelScope.launch {
            repository.deleteImage(image)
        }
    }

    suspend fun deleteUnused() {
        currentNote?.let { repository.deleteNoteWithImages(it) }
    }

    /**
     * Methods for [navigateBack] LiveData
     * it triggers closure of [com.example.noteexample.ui.EditNoteFragment]
     */

    fun onStartNavigating() {
        _navigateBack.value = true
    }

    fun onDoneNavigating() {
        _navigateBack.value = false
    }
}