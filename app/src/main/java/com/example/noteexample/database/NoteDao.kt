package com.example.noteexample.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNote(note : Note)
    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()
    @Query("SELECT * from note_table")
    fun getAllNotes() : LiveData<List<Note>>
    @Delete
    suspend fun deleteNotes(noteList: List<Note>)
    @Delete
    suspend fun deleteOneNote(note: Note)
    @Query("SELECT * from note_table WHERE id = :key")
    suspend fun getNote(key: Int) : Note
    @Query("SELECT * FROM note_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastNote(): Note
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteContent(noteContent: NoteContent)
    @Delete
    suspend fun deleteNoteContent(noteContentList: List<NoteContent>)
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNoteContent(noteContent: NoteContent)
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNoteContentList(noteContent: List<NoteContent>)
    @Query("DELETE FROM note_content")
    suspend fun deleteAllNoteContent()
    @Query("SELECT * from note_content")
    fun getAllNoteContent(): LiveData<List<NoteContent>>
    @Query("SELECT * from note_content WHERE id = :key")
    fun selectNoteContent(key: Int) : LiveData<NoteContent>
}