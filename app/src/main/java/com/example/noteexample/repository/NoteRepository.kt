package com.example.noteexample.repository

import androidx.lifecycle.LiveData
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteDao

class NoteRepository (private val noteDao: NoteDao){

    val allNotes : LiveData<List<Note>> = noteDao.getAllNotes()
    suspend fun insert(note : Note){
        noteDao.insert(note)
    }
    suspend fun deleteAll(){
        noteDao.deleteAll()
    }
    suspend fun deleteNote(noteList: List<Note>){
        noteDao.deleteNote(noteList)
    }
    fun selectNote(key: Int) : LiveData<Note>{
        return noteDao.selectNote(key)
    }
    suspend fun updateNote(note: Note){
        noteDao.updateNote(note)
    }
}