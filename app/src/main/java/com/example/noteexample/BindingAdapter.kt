package com.example.noteexample

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
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
@BindingAdapter("noteTextVisibility")
    fun TextView.setNoteVisibility(note: Note?){
        note?.let {
            if (it.note.isEmpty()) visibility = View.GONE
            else text = it.note
        }
    }
