package com.example.noteexample.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.FirstNote
import com.example.noteexample.databinding.RecyclerItemSimpleFirstNoteEditBinding
import javax.inject.Inject

class FirstNoteSimpleViewAdapter @Inject constructor():
    ListAdapter<FirstNote, FirstNoteSimpleViewAdapter.SimpleViewHolder>(FirstNoteDiffCallback()) {

    private val _simpleHolder = MutableLiveData<SimpleViewHolder>()
    val simpleHolder: LiveData<SimpleViewHolder> = _simpleHolder

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding: RecyclerItemSimpleFirstNoteEditBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.recycler_item_simple_first_note_view,
                parent,
                false
            )
        return SimpleViewHolder(binding)
    }

    inner class SimpleViewHolder(val binding: RecyclerItemSimpleFirstNoteEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            binding.firstNote = firstNote
            _simpleHolder.value = this
            binding.executePendingBindings()
        }
    }
}