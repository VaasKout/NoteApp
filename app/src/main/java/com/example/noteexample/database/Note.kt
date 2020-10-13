package com.example.noteexample.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "note_table")
data class Note (
    @PrimaryKey(autoGenerate = true) var id : Int = 0,
    @ColumnInfo(name = "title") val title : String = "",
    @ColumnInfo(name = "isChecked") var isChecked: Boolean = false
)

//TODO make second @Entity, add it into db

data class NoteContent(
    val note: String = "",
    val photoPath: String = ""
)