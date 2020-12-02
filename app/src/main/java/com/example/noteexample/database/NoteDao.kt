package com.example.noteexample.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Transaction
    @Query("SELECT * from header_table")
    fun getAllNotes(): LiveData<List<NoteWithImages>>

    @Transaction
    @Query("SELECT * from header_table ORDER BY position DESC")
    suspend fun getAllDESCSortedNotes(): List<NoteWithImages>

    @Transaction
    @Query("SELECT * from header_table ORDER BY position ASC")
    suspend fun getAllASCSortedNotes(): List<NoteWithImages>

    @Transaction
    @Query("SELECT * from header_table ORDER BY noteID DESC LIMIT 1")
    suspend fun getLastNote(): NoteWithImages

    @Transaction
    @Query("SELECT * from header_table ORDER BY noteID DESC LIMIT 1")
    fun getLastNoteLiveData(): LiveData<NoteWithImages>

    @Transaction
    @Query("SELECT * from header_table WHERE noteID = :key")
    suspend fun getNote(key: Long): NoteWithImages

    @Transaction
    @Query("SELECT * from header_table WHERE noteID = :key")
    fun getNoteLiveData(key: Long): LiveData<NoteWithImages>

    @Transaction
    @Query("DELETE FROM header_table")
    suspend fun deleteAllNotes()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNote(header: Header)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateNote(header: Header)

    @Update
    suspend fun updateNoteList(noteList: List<Header>)

    @Delete
    suspend fun deleteNote(header: Header)

    @Delete
    suspend fun deleteNoteList(headerList: List<Header>)


    @Insert
    suspend fun insertImages(images: List<Image>)

    @Insert
    suspend fun insertImage(image: Image)

    @Update
    suspend fun updateImages(images: List<Image>)

    @Update
    suspend fun updateImage(image: Image)

    @Transaction
    @Query("DELETE FROM image_table")
    suspend fun deleteAllImages()

    @Delete
    suspend fun deleteImage(image: Image)

    @Delete
    suspend fun deleteImages(images: List<Image>)


    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFlags(flags: Flags)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun updateFlags(flags: Flags)

    @Query("SELECT * from flags_table WHERE id = 0")
    fun getFlags(): LiveData<Flags>
}