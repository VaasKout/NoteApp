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
        if (this is EditText) {
            setText(it.title)
        } else {
            if (it.title.isEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = it.title
            }
        }
    }
}


@BindingAdapter("firstNoteText")
fun TextView.firstNoteText(note: Note?) {
    note?.let {
        if (this is EditText) {
            setText(it.firstNote)
        } else {
            if (it.firstNote.isEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = it.firstNote
            }
        }
    }
}

@BindingAdapter("photoNoteText")
fun TextView.photoNoteText(data: NoteContent?) {
    data?.let {
        if (this is EditText) {
            setText(it.note)
        } else {
            visibility = View.VISIBLE
            text = it.note
        }
    }
}

@BindingAdapter("imageUrl")
fun ImageView.bindImage(imgUrl: String?) {
    visibility = View.GONE
    if (!imgUrl.isNullOrEmpty()) {
            Glide.with(this.context)
                .load(imgUrl)
                .into(this)
            visibility = View.VISIBLE
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
