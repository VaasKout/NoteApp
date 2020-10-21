package com.example.noteexample

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.example.noteexample.database.GalleryData
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.google.android.material.card.MaterialCardView
import org.w3c.dom.Text

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
        if (it.note.isEmpty()) visibility = View.GONE
        else text = it.note
    }
}

@BindingAdapter("imageUrl")
fun ImageView.bindImage(imgUrl: String?){
    imgUrl?.let {
       val imgUri = imgUrl.toUri().buildUpon().scheme("content").build()
        GlideApp.with(this.context)
                .load(imgUri)
                .into(this)
    }
}
