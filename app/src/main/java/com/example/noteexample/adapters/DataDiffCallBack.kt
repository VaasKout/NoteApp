package com.example.noteexample.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.noteexample.database.FirstNote
import com.example.noteexample.database.Header
import com.example.noteexample.database.Image
import com.example.noteexample.database.NoteWithImages

/**
 * DiffUtil and data class for adapters with header
 *
 * @see com.example.noteexample.adapters.EditSimpleNoteAdapter
 * @see com.example.noteexample.adapters.ViewSimpleNoteAdapter
 */
data class NoteWithImagesRecyclerItems(
    val header: Header? = null,
    var firstNote: FirstNote? = null,
    var image: Image? = null,
)

class NoteWithImagesDiffCallback :
    DiffUtil.ItemCallback<NoteWithImagesRecyclerItems>() {
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


class FirstNoteDiffCallback : DiffUtil.ItemCallback<FirstNote>() {
    override fun areItemsTheSame(oldItem: FirstNote, newItem: FirstNote): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FirstNote, newItem: FirstNote): Boolean {
        return oldItem == newItem
    }

}


class NoteDiffCallBack : DiffUtil.ItemCallback<NoteWithImages>() {
    override fun areItemsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
        return oldItem == newItem
    }
}

