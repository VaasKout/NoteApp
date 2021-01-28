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
    var deleteAllowed = true
    var backPressed = false
    var itemListSame = false

    //Variables
    var position = 0
    var startNote: NoteWithImages? = null
    var currentNote: NoteWithImages? = null
        set(value) {
            field = value
            sortItems(field)
        }
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

    fun createNoteList() {
        noteList = mutableListOf()
        currentNote?.let { item ->
            noteList.add(0, NoteWithImagesRecyclerItems(header = item.header))
            val size = item.notes.size + item.images.size
            for (i in 0 until size) {
                item.notes.forEach { note ->
                    if (note.notePos == i) {
                        noteList.add(
                            NoteWithImagesRecyclerItems(
                                firstNote = note
                            )
                        )
                    }
                }
                item.images.forEach { image ->
                    if (image.imgPos == i) {
                        noteList.add(
                            NoteWithImagesRecyclerItems(
                                image = image
                            )
                        )
                    }
                }
            }
        }
    }

    private fun sortItems(noteWithImages: NoteWithImages?) {
        noteWithImages?.let {
            it.notes = noteWithImages.notes.sortedBy { note -> note.notePos }
            it.images = noteWithImages.images.sortedBy { image -> image.imgPos }
        }
    }

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
                val firstNote = if (it.notes.isNotEmpty()) {
                    FirstNote(
                        notePos = pos,
                        parentNoteID = it.header.headerID,
                        todoItem = it.notes[0].todoItem
                    )
                } else {
                    FirstNote(
                        notePos = pos,
                        parentNoteID = it.header.headerID
                    )
                }

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
                repository.updateImages(imgList)
                repository.updateFirstNotes(firstNoteList)
                itemListSame = false
            }
        }
    }

    private suspend fun updateCurrentNoteSuspend() {
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


    fun updateFirstNote(firstNote: FirstNote?) {
        viewModelScope.launch {
            firstNote?.let {
                repository.updateFirstNote(it)
            }
        }
    }

    suspend fun deleteCurrentNote() {
        currentNote?.let {
            repository.deleteNoteWithImages(it)
        }
    }

//    suspend fun deleteNoteWithImages(noteWithImages: NoteWithImages?) {
//        noteWithImages?.let {
//            repository.deleteNoteWithImages(it)
//        }
//    }

    private suspend fun decreasePositions(pos: Int) {
        currentNote?.let {
            it.notes.forEach { firstNote ->
                if (firstNote.notePos > pos) {
                    firstNote.notePos -= 1
                    repository.updateFirstNote(firstNote)
                }
            }
            it.images.forEach { image ->
                if (image.imgPos > pos) {
                    image.imgPos -= 1
                    repository.updateImage(image)
                }
            }
        }
    }

    private suspend fun increasePositions(pos: Int) {
        currentNote?.let {
            it.notes.forEach { firstNote ->
                if (firstNote.notePos >= pos) {
                    firstNote.notePos += 1
                    repository.updateFirstNote(firstNote)
                }
            }
            it.images.forEach { image ->
                if (image.imgPos >= pos) {
                    image.imgPos += 1
                    repository.updateImage(image)
                }
            }
        }
    }

    fun deleteFirstNote(firstNote: FirstNote) {
        deleteAllowed = false
        viewModelScope.launch {
            val pos = firstNote.notePos
            repository.deleteFirstNote(firstNote)
            decreasePositions(pos)
            deleteAllowed = true
        }
    }

    fun deleteImage(image: Image) {
        viewModelScope.launch {
            decreasePositions(image.imgPos)
            repository.deleteImage(image)
        }
    }


    suspend fun checkEmptyNoteAndDelete() {
        currentNote?.let {
            if (it.header.title.isEmpty() &&
                it.notes.none { note -> note.text.isNotEmpty() } &&
                it.images.isEmpty()
            ) {
                repository.deleteNoteWithImages(it)
            }
            it.notes.forEach { note ->
                if (!note.todoItem && note.text.isEmpty()) {
                    repository.deleteFirstNote(note)
                } else if (note.todoItem && note.text.isEmpty()) {
                    note.text = " "
                    repository.updateFirstNote(note)
                }
            }
            it.images.forEach { img ->
                if (img.hidden && img.signature.isEmpty()) {
                    repository.deleteImage(img)
                }
            }
        }
    }

    fun checkStartNote(): Boolean {
        currentNote?.let {
            if (startNote?.header?.title != it.header.title ||
                startNote?.notes != it.notes ||
                startNote?.images != it.images
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Methods for [navigateBack] LiveData
     * it triggers closure of [com.example.noteexample.ui.EditNoteFragment]
     */

    fun onStartNavigating() {
        backPressed = true
        _navigateBack.value = true
    }

    fun onDoneNavigating() {
        _navigateBack.value = false
    }

    suspend fun changeListMod() {
        currentNote?.notes?.let {
            it.forEach { firstNote ->
                firstNote.todoItem = !firstNote.todoItem
            }
            repository.updateFirstNotes(it)
        }
    }
}