package com.example.noteexample.repository

import androidx.lifecycle.LiveData
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteDao

class NoteRepository(private val noteDao: NoteDao) {

    //    val allNotes = noteDao.getAllNotes()
    val allDESCSortedNotes: LiveData<List<Note>> = noteDao.getAllDESCSortedNotes()
    val allASCSortedNotes: LiveData<List<Note>> = noteDao.getAllASCSortedNotes()
    suspend fun getNote(key: Int): Note = noteDao.getNote(key)

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }

    suspend fun deleteNoteList(noteList: List<Note>) {
        noteDao.deleteNoteList(noteList)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun updateNoteList(noteList: List<Note>) {
        noteDao.updateNoteList(noteList)
    }

    suspend fun getLastNote() = noteDao.getLastNote()


    val allNoteContent: LiveData<List<NoteContent>> = noteDao.getAllNoteContent()

    suspend fun allNoteContentSimpleList(): List<NoteContent> =
        noteDao.getAllNoteContentSimpleList()

    suspend fun getNoteContent(key: Int): NoteContent = noteDao.getNoteContent(key)

    suspend fun insertNoteContent(noteContent: NoteContent) {
        noteDao.insertNoteContent(noteContent)
    }

    suspend fun insertNoteContentList(noteContentList: List<NoteContent>) {
        noteDao.insertNoteContentList(noteContentList)
    }

    suspend fun deleteAllNoteContent() {
        noteDao.deleteAllNoteContent()
    }

    suspend fun deleteNoteContentList(noteContentList: List<NoteContent>) {
        noteDao.deleteNoteContentList(noteContentList)
    }

    suspend fun deleteNoteContent(noteContent: NoteContent) {
        noteDao.deleteNoteContent(noteContent)
    }

    suspend fun updateNoteContent(noteContent: NoteContent) {
        noteDao.updateNoteContent(noteContent)
    }

    suspend fun updateNoteContentList(noteContent: List<NoteContent>) {
        noteDao.updateNoteContentList(noteContent)
    }
}