package com.example.noteexample.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note : Note)
    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()
    @Query("SELECT * from note_table")
    fun getAllNotes() : LiveData<List<Note>>
    @Delete
    suspend fun deleteNote(noteList: List<Note>)
    @Query("SELECT * from note_table WHERE id = :key")
    fun selectNote(key: Int) : LiveData<Note>
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteContent(noteContent: NoteContent)
    @Delete
    suspend fun deleteNoteContent(noteContentList: List<NoteContent>)
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNoteContent(noteContent: NoteContent)
    @Query("DELETE FROM note_content")
    suspend fun deleteAllNoteContent()
    @Query("SELECT * from note_content")
    fun getAllNoteContent(): LiveData<List<NoteContent>>
    @Query("SELECT * from note_content WHERE id = :key")
    fun selectNoteContent(key: Int) : LiveData<NoteContent>
}