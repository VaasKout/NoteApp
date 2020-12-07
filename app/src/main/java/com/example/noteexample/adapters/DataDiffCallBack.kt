package com.example.noteexample.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.noteexample.database.Header
import com.example.noteexample.database.Image

/**
 * DiffUtil and data class for adapters with header
 *
 * @see com.example.noteexample.adapters.OneNoteEditAdapter
 * @see com.example.noteexample.adapters.OneNoteViewAdapter
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

