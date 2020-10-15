package com.example.noteexample.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "note_table")
class Note (
    @PrimaryKey(autoGenerate = true) var id : Int = 0,
    @ColumnInfo(name = "title") val title : String = "",
    @ColumnInfo(name = "isChecked") var isChecked: Boolean = false,
)

@Entity(tableName = "note_content")
data class NoteContent(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "note_id") val noteId: Int,
    @ColumnInfo(name = "note") val note: String = "",
    @ColumnInfo(name = "photoPath") val photoPath: String = ""
)