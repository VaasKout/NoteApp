package com.example.noteexample.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.noteexample.database.Note

class NoteDiffCallBack : DiffUtil.ItemCallback<Note>(){
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}