package com.example.noteexample.utils

import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent

data class DataItem(
    val note: Note? = null,
    val noteContent: NoteContent? = null,
)