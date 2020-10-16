package com.example.noteexample.repository

import androidx.lifecycle.LiveData
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.database.NoteDao

class NoteRepository (private val noteDao: NoteDao){

    val allNotes : LiveData<List<Note>> = noteDao.getAllNotes()

    suspend fun insertNote(note : Note){
        noteDao.insertNote(note)
    }
    suspend fun deleteAllNotes(){
        noteDao.deleteAllNotes()
    }
    suspend fun deleteNote(noteList: List<Note>){
        noteDao.deleteNote(noteList)
    }
    suspend fun updateNote(note: Note){
        noteDao.updateNote(note)
    }
    fun selectNote(key: Int) : LiveData<Note>{
        return noteDao.selectNote(key)
    }

    val allNoteContent: LiveData<List<NoteContent>> = noteDao.getAllNoteContent()

    suspend fun insertNoteContent(noteContent: NoteContent){
        noteDao.insertNoteContent(noteContent)
    }
    suspend fun deleteAllNoteContent(){
        noteDao.deleteAllNoteContent()
    }
    suspend fun deleteNoteContent(noteContentList: List<NoteContent>){
        noteDao.deleteNoteContent(noteContentList)
    }
    suspend fun updateNoteContent(noteContent: NoteContent){
        noteDao.updateNoteContent(noteContent)
    }
    fun selectNoteContent(key: Int){
        noteDao.selectNoteContent(key)
    }
}