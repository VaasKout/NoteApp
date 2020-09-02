package com.example.noteexample.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "note_table")
data class Note (
    @PrimaryKey(autoGenerate = true) var id : Int = 0,
    @ColumnInfo(name = "title") val title : String = "",
    @ColumnInfo(name = "content") val note : String = "",
    @ColumnInfo(name = "isChecked") var isChecked: Boolean = false)