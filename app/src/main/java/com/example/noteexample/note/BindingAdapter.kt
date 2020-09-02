package com.example.noteexample.note

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.noteexample.database.Note
import com.google.android.material.card.MaterialCardView


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


     @BindingAdapter(value = ["android:note", "android:onLongClick"], requireAll = true)
    fun MaterialCardView.setCheckedState(note: Note?, clickListener: NoteListener){
        this.setOnLongClickListener {
            note?.let { note ->
                this.isChecked = !this.isChecked
                note.isChecked = this.isChecked
                clickListener.onLongClick(note)
        }
            true
        }
     }
