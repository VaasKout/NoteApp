package com.example.noteexample.allNotes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.databinding.RecyclerMainItemBinding

class NoteAdapter :
    ListAdapter<NoteWithImages, NoteAdapter.NoteViewHolder>(NoteDiffCallBack()) {
    /**
     * [holder] gets instance of InsertUpdateViewHolder and observed in [AllNotesFragment]
     * to set clickListeners for recycler items
     */
    private val _holder = MutableLiveData<NoteViewHolder>()
    val holder: LiveData<NoteViewHolder> = _holder

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        /**
         * Use only DataBindingUtil else layout options crash in recycler_main_item
         */
        val binding: RecyclerMainItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.recycler_main_item, parent, false)
        return NoteViewHolder(binding)
    }

    inner class NoteViewHolder(val binding: RecyclerMainItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Checked state of card depends on [Note.isChecked] state
         */
        fun bind(noteWithImages: NoteWithImages) {
            _holder.value = this
            binding.note = noteWithImages.note
            binding.mainCard.isChecked = noteWithImages.note.isChecked
            binding.executePendingBindings()
        }
    }
}

class NoteDiffCallBack : DiffUtil.ItemCallback<NoteWithImages>() {
    override fun areItemsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NoteWithImages, newItem: NoteWithImages): Boolean {
        return oldItem == newItem
    }
}





