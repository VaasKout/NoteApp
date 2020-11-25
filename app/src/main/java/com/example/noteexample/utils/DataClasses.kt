package com.example.noteexample.utils

import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent

data class DataItem(
    val note: Note? = null,
    var noteContent: NoteContent? = null,
)

data class GalleryData (
    val imgSrcUrl: String,
    var isChecked: Boolean = false,
)