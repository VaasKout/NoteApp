package com.example.noteexample.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.noteexample.database.*

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

class GalleryDiffCallBack : DiffUtil.ItemCallback<GalleryData>() {
    override fun areItemsTheSame(oldItem: GalleryData, newItem: GalleryData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: GalleryData, newItem: GalleryData): Boolean {
        return oldItem == newItem
    }

}

class ImageDiffCallback : DiffUtil.ItemCallback<Image>() {
    override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
        return oldItem.imgPos == newItem.imgPos
    }

    override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
        return oldItem == newItem
    }
}

