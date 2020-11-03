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
            if (it.title.isEmpty()) visibility = View.GONE
            else text = it.title
        }
    }
}


@BindingAdapter("firstNoteText")
fun TextView.firstNoteText(note: Note?) {
    note?.let {
        if (this is EditText) {
            setText(it.firstNote)
        } else {
            if (it.firstNote.isEmpty()) visibility = View.GONE
            else text = it.firstNote
        }
    }
}

@BindingAdapter("photoNoteText")
fun TextView.photoNoteText(data: NoteContent?) {
    data?.let {
        if (this is EditText) {
            setText(it.note)
        } else {
            text = it.note
            visibility = View.VISIBLE
        }
    }
}

@BindingAdapter(value = ["imageUrl", "load"], requireAll = true)
fun ImageView.bindImage(imgUrl: String?, load: Boolean) {
    visibility = if (!imgUrl.isNullOrEmpty()) {
        if (load) {
            Glide.with(this.context)
                .load(imgUrl)
                .into(this)
            View.VISIBLE
        } else {
            View.VISIBLE
        }
    } else {
        View.GONE
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
