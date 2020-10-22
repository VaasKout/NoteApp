package com.example.noteexample

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent

/**
 * BindingAdapter handle with redundant space in MaterialCardView if one of views is empty
 *
 * android:id="@+id/titleRecyclerItem"
 * app:titleTextVisibility="@{note}"
 *
 * android:id="@+id/noteRecyclerItem"
 * app:noteTextVisibility="@{note}"
 *
 * note is instance of Note from database
 */

@GlideModule
class GlideAppModule : AppGlideModule()

@BindingAdapter("titleTextVisibility")
    fun TextView.setTitleVisibility(note: Note?){
    note?.let {
        if (it.title.isEmpty()) visibility = View.GONE
        else text = it.title
    }
}

@BindingAdapter("noteTextVisibility")
    fun TextView.setNoteVisibility(data: NoteContent?){
    data?.let {
        if(it.note.isNotEmpty()){
            text = it.note
            visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("imageUrl")
fun ImageView.bindImage(imgUrl: String?){
        if (!imgUrl.isNullOrEmpty()){
            val imgUri = imgUrl.toUri().buildUpon().scheme("content").build()
            GlideApp.with(this.context)
                .load(imgUri)
                .into(this)
            visibility = View.VISIBLE
        }
}

@BindingAdapter(value = ["dataIsEmpty", "titleIsEmpty"], requireAll = true)
fun View.setViewVisibility(data: NoteContent?, note: Note?){
    note?.let {
        data?.let {
            visibility = View.VISIBLE
        }
    }
}
