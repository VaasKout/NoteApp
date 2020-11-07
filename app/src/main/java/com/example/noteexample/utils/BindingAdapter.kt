package com.example.noteexample.utils

import android.util.Log
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
    data?.let {
        when {
            this is EditText -> {
                setText(it.note)
            }
            it.note.isEmpty() -> {
                visibility = View.GONE
            }
            else -> {
                visibility = View.VISIBLE
                text = it.note
            }
        }
    }
}

@BindingAdapter("imageUrl")
fun ImageView.bindImage(imgUrl: String?) {
    if (!imgUrl.isNullOrEmpty()) {
        Glide.with(this.context)
            .load(imgUrl)
            .into(this)
    }
}

@BindingAdapter(value = ["dataIsEmpty", "titleIsEmpty"], requireAll = true)
fun View.setViewVisibility(data: NoteContent?, note: Note?) {
    note?.let {
        data?.let {
            visibility = View.VISIBLE
        }
    }
}
