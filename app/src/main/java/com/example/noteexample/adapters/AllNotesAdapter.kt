package com.example.noteexample.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.databinding.RecyclerItemMainBinding

//TODO adapter depends on type (simple | todo)

class NoteAdapter :
    ListAdapter<NoteWithImages, NoteAdapter.NoteViewHolder>(NoteDiffCallBack()) {

    /**
     * [holder] gets instance of InsertUpdateViewHolder and observed in
     * [com.example.noteexample.ui.AllNotesFragment] to set clickListeners for recycler items
     */

    private val _holder = MutableLiveData<NoteViewHolder>()
    val holder: LiveData<NoteViewHolder> = _holder

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        /**
         * Use only DataBindingUtil else layout options crash in recycler_main_item
         */
        val binding: RecyclerItemMainBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.recycler_item_main, parent, false)
        return NoteViewHolder(binding)
    }

    inner class NoteViewHolder(val binding: RecyclerItemMainBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            _holder.value = this
            binding.executePendingBindings()
        }
    }
}
