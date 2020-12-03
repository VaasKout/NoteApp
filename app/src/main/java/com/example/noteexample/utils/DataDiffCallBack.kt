package com.example.noteexample.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.noteexample.database.Header
import com.example.noteexample.database.Image

/**
 * DiffUtil and data class for adapters with header
 *
 * @see com.example.noteexample.editNote.OneNoteEditAdapter
 * @see com.example.noteexample.oneNote.OneNoteViewAdapter
 */
data class NoteWithImagesRecyclerItems(
    val header: Header? = null,
    var image: Image? = null,
)

class DataDiffCallBack : DiffUtil.ItemCallback<NoteWithImagesRecyclerItems>() {
    override fun areItemsTheSame(
        oldItem: NoteWithImagesRecyclerItems,
        newItem: NoteWithImagesRecyclerItems
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: NoteWithImagesRecyclerItems,
        newItem: NoteWithImagesRecyclerItems
    ): Boolean {
        return oldItem == newItem
    }

}

