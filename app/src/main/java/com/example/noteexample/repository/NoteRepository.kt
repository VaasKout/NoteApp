package com.example.noteexample.repository

import androidx.lifecycle.LiveData
import com.example.noteexample.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao) {

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
    suspend fun getLastNote() = withContext(Dispatchers.IO) { noteDao.getLastNote() }


    suspend fun allDESCSortedNotes(): List<NoteWithImages> =
        withContext(Dispatchers.IO) { noteDao.getAllDESCSortedNotes() }

    suspend fun allASCSortedNotes(): List<NoteWithImages> =
        withContext(Dispatchers.IO) { noteDao.getAllASCSortedNotes() }

    suspend fun getNote(key: Long): NoteWithImages =
        withContext(Dispatchers.IO) { noteDao.getNote(key) }



    suspend fun insertNoteWithImages(note: NoteWithImages) {
        withContext(Dispatchers.IO) {
            noteDao.insertHeader(note.header)
            noteDao.insertImages(note.images)
        }
    }

    suspend fun deleteAllNotesWithImages() {
        withContext(Dispatchers.IO) {
            noteDao.deleteAllNotes()
            noteDao.deleteAllImages()
        }
    }

    suspend fun deleteNoteWithImagesList(noteList: List<NoteWithImages>) {
        val notes = mutableListOf<Header>()
        val images = mutableListOf<Image>()
        withContext(Dispatchers.Default) {
            noteList.forEach {
                notes.add(it.header)
                images.addAll(it.images)
            }
        }
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteList(notes)
        }

    }

    suspend fun deleteNoteWithImages(item: NoteWithImages) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(item.header)
            noteDao.deleteImages(item.images)
        }
    }

    suspend fun updateNoteWithImages(item: NoteWithImages) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(item.header)
            noteDao.updateImages(item.images)
        }
    }

    suspend fun updateNoteWithImagesList(noteList: List<NoteWithImages>) {
        val notes = mutableListOf<Header>()
        val images = mutableListOf<Image>()
        withContext(Dispatchers.Default) {
            noteList.forEach {
                notes.add(it.header)
                images.addAll(it.images)
            }
        }
        withContext(Dispatchers.IO) {
            noteDao.updateNoteList(notes)
            noteDao.updateImages(images)
        }
    }


    /**
     * @see Header
     */
    suspend fun insertNote(header: Header) {
        withContext(Dispatchers.IO) {
            noteDao.insertHeader(header)
        }
    }




    /**
     * @see Image
     *
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

    suspend fun deleteImage(image: Image) {
        withContext(Dispatchers.IO) {
            noteDao.deleteImage(image)
        }
    }


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