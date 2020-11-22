package com.example.noteexample.repository

import androidx.lifecycle.LiveData
import com.example.noteexample.database.Flags
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes = noteDao.getAllNotes()

    suspend fun allDESCSortedNotes(): List<Note> =
        withContext(Dispatchers.IO) { noteDao.getAllDESCSortedNotes() }

    suspend fun allASCSortedNotes(): List<Note> =
        withContext(Dispatchers.IO) { noteDao.getAllASCSortedNotes() }

    suspend fun getNote(key: Int): Note =
        withContext(Dispatchers.IO) { noteDao.getNote(key) }

    suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.insertNote(note)
        }
    }

    suspend fun deleteAllNotes() {
        withContext(Dispatchers.IO) {
            noteDao.deleteAllNotes()
        }
    }

    suspend fun deleteNoteList(noteList: List<Note>) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteList(noteList)
        }

    }

    suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNote(note)

        }
    }

    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.updateNote(note)
        }

    }

    suspend fun updateNoteList(noteList: List<Note>) {
        withContext(Dispatchers.IO) {
            noteDao.updateNoteList(noteList)
        }

    }

    suspend fun getLastNote() = withContext(Dispatchers.IO) { noteDao.getLastNote() }


    val allNoteContent: LiveData<List<NoteContent>> = noteDao.getAllNoteContent()

    suspend fun allNoteContentSimpleList(): List<NoteContent> =
        withContext(Dispatchers.IO) { noteDao.getAllNoteContentSimpleList() }

    suspend fun getNoteContent(key: Int): NoteContent =
        withContext(Dispatchers.IO) { noteDao.getNoteContent(key) }

    suspend fun insertNoteContent(noteContent: NoteContent) {
        withContext(Dispatchers.IO) {
            noteDao.insertNoteContent(noteContent)
        }

    }

    suspend fun insertNoteContentList(noteContentList: List<NoteContent>) {
        withContext(Dispatchers.IO) {
            noteDao.insertNoteContentList(noteContentList)
        }

    }

    suspend fun deleteAllNoteContent() {
        withContext(Dispatchers.IO) {
            noteDao.deleteAllNoteContent()
        }

    }

    suspend fun deleteNoteContentList(noteContentList: List<NoteContent>) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteContentList(noteContentList)
        }

    }

    suspend fun deleteNoteContent(noteContent: NoteContent) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteContent(noteContent)
        }

    }

    suspend fun updateNoteContent(noteContent: NoteContent) {
        withContext(Dispatchers.IO) {
            noteDao.updateNoteContent(noteContent)
        }

    }

    suspend fun updateNoteContentList(noteContent: List<NoteContent>) {
        withContext(Dispatchers.IO) {
            noteDao.updateNoteContentList(noteContent)
        }

    }

    val flags: LiveData<Flags> = noteDao.getFlags()

    suspend fun updateFlags(flags: Flags) {
        withContext(Dispatchers.IO) {
            noteDao.updateFlags(flags)
        }
    }
}