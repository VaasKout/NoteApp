package com.example.noteexample.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    @Query("SELECT * from note_table ORDER BY position DESC")
    fun getAllDESCSortedNotes(): LiveData<List<Note>>

    @Query("SELECT * from note_table ORDER BY position ASC")
    fun getAllASCSortedNotes(): LiveData<List<Note>>

    @Query("SELECT * from note_table WHERE id = :key")
    suspend fun getNote(key: Int): Note

    @Query("SELECT * FROM note_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastNote(): Note

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNote(note: Note)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNote(note: Note)

    @Update
    suspend fun updateNoteList(noteList: List<Note>)

    @Delete
    suspend fun deleteNote(note: Note)

    @Delete
    suspend fun deleteNoteList(noteList: List<Note>)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteContent(noteContent: NoteContent)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteContentList(noteContentList: List<NoteContent>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNoteContent(noteContent: NoteContent)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNoteContentList(noteContent: List<NoteContent>)

    @Delete
    suspend fun deleteNoteContentList(noteContentList: List<NoteContent>)

    @Delete
    suspend fun deleteNoteContent(noteContent: NoteContent)

    @Query("DELETE FROM note_content")
    suspend fun deleteAllNoteContent()

    @Query("SELECT * from note_content")
    fun getAllNoteContent(): LiveData<List<NoteContent>>

    @Query("SELECT * from note_content WHERE id = :key")
    fun selectNoteContent(key: Int): LiveData<NoteContent>
}