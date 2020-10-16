package com.example.noteexample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.database.NoteContent
import com.example.noteexample.databinding.FragmentOneNoteBinding
import com.example.noteexample.databinding.RecyclerNoteContentEditItemBinding
import com.example.noteexample.databinding.RecyclerNoteContentViewItemBinding

class OneNoteEditAdapter :
    ListAdapter<NoteContent, OneNoteEditAdapter.NoteContentEditHolder>(NoteDiffCallBack()) {

    private val _holder = MutableLiveData<NoteContentEditHolder>()
    val holder: LiveData<NoteContentEditHolder> = _holder

    override fun onBindViewHolder(holder: NoteContentEditHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteContentEditHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: RecyclerNoteContentEditItemBinding =
            DataBindingUtil
                .inflate(layoutInflater, R.layout.recycler_note_content_edit_item, parent, false)
        return NoteContentEditHolder(binding)
    }

    inner class NoteContentEditHolder(val binding: RecyclerNoteContentEditItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(noteContent: NoteContent) {
            _holder.value = this
        }
    }
}

class NoteDiffCallBack : DiffUtil.ItemCallback<NoteContent>() {
    override fun areItemsTheSame(oldItem: NoteContent, newItem: NoteContent): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NoteContent, newItem: NoteContent): Boolean {
        return oldItem == newItem
    }
}