package com.example.noteexample.database


import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "note_table")
data class Note (
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    @ColumnInfo(name = "title") val title : String = "",
    @ColumnInfo(name = "isChecked") var isChecked: Boolean = false,
)

@Entity(tableName = "note_content")
data class NoteContent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "note_id") val noteId: Int,
    @ColumnInfo(name = "note") val note: String = "",
    @ColumnInfo(name = "photoPath") val photoPath: String,
)