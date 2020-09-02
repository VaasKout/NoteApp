package com.example.noteexample.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note : Note)
    @Query("DELETE FROM note_table")
    suspend fun deleteAll()
    @Query("SELECT * from note_table")
    fun getAllNotes() : LiveData<List<Note>>
    @Delete
    suspend fun deleteNote(noteList: List<Note>)
    @Query("SELECT * from note_table WHERE id = :key")
    fun selectNote(key: Int) : LiveData<Note>
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNote(note: Note)
}