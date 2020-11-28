package com.example.noteexample.repository

import androidx.lifecycle.LiveData
import com.example.noteexample.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes = noteDao.getAllNotes()
    fun getNoteLiveData(id: Long): LiveData<NoteWithImages> = noteDao.getNoteLiveData(id)
    fun getLastLiveData(): LiveData<NoteWithImages> = noteDao.getLastNoteLiveData()
    suspend fun getLastNote() = withContext(Dispatchers.IO) { noteDao.getLastNote() }

    suspend fun allDESCSortedNotes(): List<NoteWithImages> =
        withContext(Dispatchers.IO) { noteDao.getAllDESCSortedNotes() }

    suspend fun allASCSortedNotes(): List<NoteWithImages> =
        withContext(Dispatchers.IO) { noteDao.getAllASCSortedNotes() }

    suspend fun getNote(key: Long): NoteWithImages =
        withContext(Dispatchers.IO) { noteDao.getNote(key) }

    suspend fun insertNote(note:Note){
        withContext(Dispatchers.IO){
            noteDao.insertNote(note)
        }
    }

    suspend fun insertNoteWithImages(note: NoteWithImages) {
        withContext(Dispatchers.IO) {
            noteDao.insertNote(note.note)
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
        val notes = mutableListOf<Note>()
        val images = mutableListOf<Image>()
        withContext(Dispatchers.Default){
            noteList.forEach {
                notes.add(it.note)
                images.addAll(it.images)
            }
        }
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteList(notes)
        }

    }

    suspend fun deleteNoteWithImages(item: NoteWithImages) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(item.note)
            noteDao.deleteImages(item.images)
        }
    }

    suspend fun updateNoteWithImages(item: NoteWithImages) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(item.note)
            noteDao.updateImages(item.images)
        }
    }

    suspend fun updateNoteWithImagesList(noteList: List<NoteWithImages>) {
        val notes = mutableListOf<Note>()
        val images = mutableListOf<Image>()
        withContext(Dispatchers.Default){
            noteList.forEach {
                notes.add(it.note)
                images.addAll(it.images)
            }
        }
        withContext(Dispatchers.IO) {
            noteDao.updateNoteList(notes)
            noteDao.updateImages(images)
        }
    }


    suspend fun insertImages(images: List<Image>) {
        withContext(Dispatchers.IO) {
            noteDao.insertImages(images)
        }
    }

    suspend fun insertImage(image: Image){
        withContext(Dispatchers.IO){
            noteDao.insertImage(image)
        }
    }

    suspend fun updateImage(image: Image){
        withContext(Dispatchers.IO){
            noteDao.updateImage(image)
        }
    }

    suspend fun deleteImage(image: Image) {
        withContext(Dispatchers.IO) {
            noteDao.deleteImage(image)
        }
    }


    val flags: LiveData<Flags> = noteDao.getFlags()

    suspend fun updateFlags(flags: Flags) {
        withContext(Dispatchers.IO) {
            noteDao.updateFlags(flags)
        }
    }
}