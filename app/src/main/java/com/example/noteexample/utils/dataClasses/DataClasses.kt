package com.example.noteexample.utils.dataClasses

import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent

data class DataItem(
    val note: Note? = null,
    val noteContent: NoteContent? = null,
)

data class GalleryData (
    val imgSrcUrl: String,
    var isChecked: Boolean = false,
)