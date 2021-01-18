package com.example.noteexample.viewmodels

import androidx.lifecycle.*
import com.example.noteexample.database.Header
import com.example.noteexample.database.Image
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.repository.NoteRepository
import com.example.noteexample.adapters.NoteWithImagesRecyclerItems
import com.example.noteexample.database.FirstNote
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

    private var firstNoteList = mutableListOf<FirstNote>()
    private var imgList = mutableListOf<Image>()
    fun swapItems(from: Int, to: Int) {
        itemListSame = true
        val tmpItem = noteList[from + 1].copy()
        noteList[from + 1] = noteList[to + 1].copy()
        noteList[to + 1] = tmpItem
        firstNoteList = mutableListOf()
        imgList = mutableListOf()

        noteList.forEachIndexed { index, item ->
            item.firstNote?.let {
                it.notePos = index - 1
                firstNoteList.add(it)
            }
            item.image?.let {
                it.imgPos = index - 1
                imgList.add(it)
            }
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
                        imgPos = it.images.size,
                        parentImgNoteID = it.header.headerID,
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
            repository.insertHeader(header)
            val firstNote = FirstNote(
                notePos = 0,
                parentNoteID = repository.getLastNote().header.headerID
            )
            repository.insertFirstNote(firstNote)
            startNote = repository.getLastNote()
            repository.getLastLiveData()
        }
    }

    /**
     * Database methods
     */

    suspend fun insertNoteWithImages(noteWithImages: NoteWithImages?) {
        noteWithImages?.let {
            repository.insertNoteWithImages(noteWithImages)
        }
    }

    fun insertNewFirstNote() {
        viewModelScope.launch {
            updateCurrentNoteSuspend()
            currentNote?.let {
                var pos = 0
                for (i in it.notes.indices) {
                    if (i != it.notes[i].notePos) {
                        break
                    } else {
                        pos = i + 1
                    }
                }
                increasePositions(pos)
                val firstNote = FirstNote(
                    notePos = pos,
                    parentNoteID = it.header.headerID
                )
                repository.insertFirstNote(firstNote)
            }
        }
    }


    fun updateCurrentNote() {
        viewModelScope.launch {
            updateCurrentNoteSuspend()
        }
    }

    fun updateAfterSwap() {
        viewModelScope.launch {
            currentNote?.let {
                repository.deleteNoteImagesAndFirstNotes(it.header.headerID)
//                it.notes = firstNoteList
//                it.images = imgList
                repository.insertImages(imgList)
                repository.insertFirstNotes(firstNoteList)
            }
        }
    }

    suspend fun updateCurrentNoteSuspend() {
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


    fun updateFirstNote(list: List<FirstNote>) {
        viewModelScope.launch {
            repository.updateFirstNotes(list)
        }
    }


    suspend fun deleteNoteWithImages(noteWithImages: NoteWithImages?) {
        noteWithImages?.let {
            repository.deleteNoteWithImages(it)
        }
    }

    private suspend fun decreasePositions(pos: Int) {
        currentNote?.let {
            it.notes.forEach { firstNote ->
                if (firstNote.notePos > pos) {
                    repository.deleteFirstNote(firstNote)
                    firstNote.notePos -= 1
                    repository.insertFirstNote(firstNote)
                }
            }
            it.images.forEach { image ->
                if (image.imgPos > pos) {
                    repository.deleteImage(image)
                    image.imgPos -= 1
                    repository.insertImage(image)
                }
            }
        }
    }

    private suspend fun increasePositions(pos: Int) {
        currentNote?.let {
            it.notes.forEach { firstNote ->
                if (firstNote.notePos >= pos) {
                    repository.deleteFirstNote(firstNote)
                    firstNote.notePos += 1
                    repository.insertFirstNote(firstNote)
                }
            }
            it.images.forEach { image ->
                if (image.imgPos >= pos) {
                    repository.deleteImage(image)
                    image.imgPos += 1
                    repository.insertImage(image)
                }
            }
        }
    }

    fun deleteFirstNote(firstNote: FirstNote) {
        viewModelScope.launch {
            val pos = firstNote.notePos
            repository.deleteFirstNote(firstNote)
            decreasePositions(pos)
        }
    }

    fun deleteImage(image: Image) {
        viewModelScope.launch {
            decreasePositions(image.imgPos)
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