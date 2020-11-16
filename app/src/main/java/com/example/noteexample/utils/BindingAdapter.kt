package com.example.noteexample.utils

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent

//@GlideModule
//class GlideAppModule : AppGlideModule()

@BindingAdapter("titleText")
fun TextView.titleText(note: Note?) {
    note?.let {
        when {
            this is EditText -> {
                setText(it.title)
            }
            it.title.isEmpty() -> {
                visibility = View.GONE
            }
            else -> {
                visibility = View.VISIBLE
                text = it.title
            }
        }
    }
}


@BindingAdapter("firstNoteText")
fun TextView.firstNoteText(note: Note?) {
    note?.let {
        when {
            this is EditText -> {
                setText(it.firstNote)
            }
            it.firstNote.isEmpty() -> {
                visibility = View.GONE
            }
            else -> {
                visibility = View.VISIBLE
                text = it.firstNote
            }
        }
    }
}

@BindingAdapter("photoNoteText")
fun TextView.photoNoteText(data: NoteContent?) {
    if (data == null) {
        visibility = View.GONE
    } else {
        when {
            this is EditText -> {
                setText(data.note)
            }
            data.note.isEmpty() -> {
                visibility = View.GONE
            }
            else -> {
                visibility = View.VISIBLE
                text = data.note
            }
        }
    }
}

@BindingAdapter("imageEditUrl")
fun ImageView.bindEditImage(imgUrl: String?) {
    if (!imgUrl.isNullOrEmpty()) {
        Glide.with(this.context)
            .load(imgUrl)
            .into(this)
    }
}

@BindingAdapter("imageViewUrl")
fun ImageView.bindViewImage(imgUrl: String?) {
    if (!imgUrl.isNullOrEmpty()) {
        visibility = View.VISIBLE
        Glide.with(this.context)
            .load(imgUrl)
            .into(this)
    } else visibility = View.GONE
}
