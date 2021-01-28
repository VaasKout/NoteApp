package com.example.noteexample.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.FirstNote
import com.example.noteexample.database.NoteWithImages
import com.example.noteexample.databinding.RecyclerItemMainBinding
import dagger.hilt.android.qualifiers.ApplicationContext

class NoteAdapter :
    ListAdapter<NoteWithImages, NoteAdapter.NoteViewHolder>(NoteDiffCallBack()) {

    val cardAdapter = FirstNoteAdapter(false)
    /**
     * [holder] gets instance of InsertUpdateViewHolder and observed in
     * [com.example.noteexample.ui.AllNotesFragment] to set clickListeners for recycler items
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
        val binding: RecyclerItemMainBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.recycler_item_main, parent, false)
        return NoteViewHolder(binding)
    }

    inner class NoteViewHolder(val binding: RecyclerItemMainBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(noteWithImages: NoteWithImages) {
            _holder.value = this

            val layoutManager = object : LinearLayoutManager(
                binding.recyclerInMainItem.context,
                RecyclerView.VERTICAL,
                false
            ) {
                override fun canScrollHorizontally(): Boolean {
                    return false
                }

                override fun canScrollVertically(): Boolean {
                    return false
                }

            }

            binding.recyclerInMainItem.apply {
                if (adapter == null) {
                    setLayoutManager(layoutManager)
                    adapter = cardAdapter
                    setHasFixedSize(true)
                }
            }
            cardAdapter.submitList(noteWithImages.notes)
            binding.executePendingBindings()
        }
    }
}
