package com.example.noteexample

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.noteexample.database.Note


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

@BindingAdapter("titleTextVisibility")
    fun TextView.setTitleVisibility(note: Note?){
    note?.let {
        if (it.title.isEmpty()) visibility = View.GONE
        else text = it.title
    }
}

//@BindingAdapter("galleryImage")
//    fun ImageView.setImage(uri: String){
//    Glide.with(this.context)
//        .load(uri)
//        .into(this)
//}
//@BindingAdapter("noteTextVisibility")
//    fun TextView.setNoteVisibility(note: Note?){
//        note?.let {
//            if (it.note.isEmpty()) visibility = View.GONE
//            else text = it.note
//        }
//    }
