package com.example.noteexample.repository

import androidx.lifecycle.LiveData
import com.example.noteexample.database.*
import com.example.noteexample.utils.Camera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject
constructor(private val noteDao: NoteDao, private val camera: Camera) {

    /**
     * Repository for Dao methods
     * @see NoteDao
     */

    /**
     * @see NoteWithImages
     */

    //    val allNotes = noteDao.getAllNotes()
    fun getNoteLiveData(id: Long): LiveData<NoteWithImages> = noteDao.getNoteLiveData(id)
    fun getLastLiveData(): LiveData<NoteWithImages> = noteDao.getLastNoteLiveData()
    suspend fun getLastNote(): NoteWithImages =
        withContext(Dispatchers.IO) {
            val noteWithImages = noteDao.getLastNote()
            noteWithImages.apply {
                this.notes = notes.sortedBy { note -> note.notePos }
                this.images = images.sortedBy { image -> image.imgPos }
            }
            noteWithImages
        }


    suspend fun allDESCSortedNotes(): List<NoteWithImages> =
        withContext(Dispatchers.IO) {
            val descSortedNotes = noteDao.getAllDESCSortedNotes()
            descSortedNotes.forEach { noteWithImages ->
                noteWithImages.notes = noteWithImages.notes.sortedBy { note -> note.notePos }
                noteWithImages.images = noteWithImages.images.sortedBy { image -> image.imgPos }
            }
            descSortedNotes
        }

    suspend fun allASCSortedNotes(): List<NoteWithImages> =
        withContext(Dispatchers.IO) {
            val ascSortedNotes = noteDao.getAllASCSortedNotes()
            ascSortedNotes.forEach { noteWithImages ->
                noteWithImages.notes = noteWithImages.notes.sortedBy { note -> note.notePos }
                noteWithImages.images = noteWithImages.images.sortedBy { img -> img.imgPos }
            }
            ascSortedNotes
        }

    suspend fun getNote(key: Long): NoteWithImages =
        withContext(Dispatchers.IO) { noteDao.getNote(key) }


    suspend fun insertNoteWithImages(note: NoteWithImages) {
        withContext(Dispatchers.IO) {
            noteDao.insertHeader(note.header)
            noteDao.insertFirstNotes(note.notes)
            noteDao.insertImages(note.images)
        }
    }

    suspend fun deleteAllNotesWithImages() {
        withContext(Dispatchers.IO) {
            noteDao.deleteAllNotes()
            noteDao.deleteAllFirstNotes()
            noteDao.deleteAllImages()
        }
    }

    suspend fun deleteNoteWithImagesList(noteList: List<NoteWithImages>) {
        val headers = mutableListOf<Header>()
        val notes = mutableListOf<FirstNote>()
        val images = mutableListOf<Image>()
        withContext(Dispatchers.Default) {
            noteList.forEach {
                headers.add(it.header)
                notes.addAll(it.notes)
                images.addAll(it.images)
            }
        }
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteList(headers)
            noteDao.deleteFirstNotes(notes)
            noteDao.deleteImages(images)
        }
    }

    suspend fun deleteNoteWithImages(item: NoteWithImages) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(item.header)
            noteDao.deleteFirstNotes(item.notes)
            noteDao.deleteImages(item.images)
        }
    }

    suspend fun updateNoteWithImages(item: NoteWithImages) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(item.header)
            noteDao.updateFirstNotes(item.notes)
            noteDao.updateImages(item.images)
        }
    }

    suspend fun updateNoteWithImagesList(noteList: List<NoteWithImages>) {
        val headers = mutableListOf<Header>()
        val notes = mutableListOf<FirstNote>()
        val images = mutableListOf<Image>()
        withContext(Dispatchers.Default) {
            noteList.forEach {
                headers.add(it.header)
                notes.addAll(it.notes)
                images.addAll(it.images)
            }
        }
        withContext(Dispatchers.IO) {
            noteDao.updateNoteList(headers)
            noteDao.updateFirstNotes(notes)
            noteDao.updateImages(images)
        }
    }

    suspend fun deleteNoteImagesAndFirstNotes(id: Long) {
        withContext(Dispatchers.IO) {
            val note = noteDao.getNote(id)
            noteDao.deleteFirstNotes(note.notes)
            noteDao.deleteImages(note.images)
        }
    }


    /**
     * @see Header
     */
    suspend fun insertHeader(header: Header) {
        withContext(Dispatchers.IO) {
            noteDao.insertHeader(header)
        }
    }

    /**
     * @see FirstNote
     */
    suspend fun insertFirstNote(firstNote: FirstNote) {
        withContext(Dispatchers.IO) {
            noteDao.insertFirstNote(firstNote)
        }
    }

    suspend fun insertFirstNotes(firstNotes: List<FirstNote>) {
        withContext(Dispatchers.IO) {
            noteDao.insertFirstNotes(firstNotes)
        }
    }

    suspend fun updateFirstNote(firstNote: FirstNote) {
        withContext(Dispatchers.IO) {
            noteDao.updateFirstNote(firstNote)
        }
    }

    suspend fun updateFirstNotes(firstNotes: List<FirstNote>) {
        withContext(Dispatchers.IO) {
            noteDao.updateFirstNotes(firstNotes)
        }
    }

    suspend fun deleteFirstNote(firstNote: FirstNote) {
        withContext(Dispatchers.IO) {
            noteDao.deleteFirstNote(firstNote)
        }
    }

    suspend fun deleteFirstNotes(firstNotes: List<FirstNote>) {
        withContext(Dispatchers.IO) {
            noteDao.deleteFirstNotes(firstNotes)
        }
    }


    /**
     * @see Image
     */
    suspend fun insertImages(images: List<Image>) {
        withContext(Dispatchers.IO) {
            noteDao.insertImages(images)
        }
    }

    suspend fun insertImage(image: Image) {
        withContext(Dispatchers.IO) {
            noteDao.insertImage(image)
        }
    }

    suspend fun updateImage(image: Image) {
        withContext(Dispatchers.IO) {
            noteDao.updateImage(image)
        }
    }

    suspend fun updateImages(images: List<Image>) {
        withContext(Dispatchers.IO) {
            noteDao.updateImages(images)
        }
    }

    suspend fun deleteImage(image: Image) {
        withContext(Dispatchers.IO) {
            noteDao.deleteImage(image)
        }
    }

    suspend fun deleteImages(images: List<Image>) {
        withContext(Dispatchers.IO) {
            noteDao.deleteImages(images)
        }
    }

    fun getGalleryData(): List<GalleryData> = camera.loadImagesFromStorage()

    /**
     * @see Flags
     */

    val flags: LiveData<Flags> = noteDao.getFlags()

    suspend fun updateFlags(flags: Flags) {
        withContext(Dispatchers.IO) {
            noteDao.updateFlags(flags)
        }
    }
}