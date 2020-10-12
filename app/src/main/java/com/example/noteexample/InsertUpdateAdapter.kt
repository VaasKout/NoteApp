package com.example.noteexample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.database.Note
import com.example.noteexample.databinding.RecyclerInsertUpdateItemBinding

//TODO make new data class with note and image, observe it here and insert it in fragments
//

class InsertUpdateAdapter :
    ListAdapter<Note, InsertUpdateAdapter.InsertUpdateViewHolder>(NoteDiffCallBack()) {

    private val _holder = MutableLiveData<InsertUpdateViewHolder>()
    val holder: LiveData<InsertUpdateViewHolder> = _holder

    override fun onBindViewHolder(holder: InsertUpdateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsertUpdateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        /**
         * Use only DataBindingUtil else layout options crash in recycler_main_item
         */
        val binding: RecyclerInsertUpdateItemBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.recycler_insert_update_item,
                parent,
                false
            )
        return InsertUpdateViewHolder(binding)
    }

    inner class InsertUpdateViewHolder(val binding: RecyclerInsertUpdateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            _holder.value = this
//            binding.note = note
//            binding.materialCard.isChecked = note.isChecked
            binding.executePendingBindings()
        }
    }
}

class NoteDiffCallBack : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}