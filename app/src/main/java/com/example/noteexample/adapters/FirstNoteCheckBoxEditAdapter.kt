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
import com.example.noteexample.databinding.RecyclerItemTodoFirstNoteEditBinding

class FirstNoteCheckBoxEditAdapter:
    ListAdapter<FirstNote, FirstNoteCheckBoxEditAdapter.CheckBoxEditHolder>(FirstNoteDiffCallback()) {

    private val _checkBoxHolder = MutableLiveData<CheckBoxEditHolder>()
    val checkBoxHolder: LiveData<CheckBoxEditHolder> = _checkBoxHolder

    override fun onBindViewHolder(holder: CheckBoxEditHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckBoxEditHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding: RecyclerItemTodoFirstNoteEditBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.recycler_item_todo_first_note_edit,
                parent,
                false
            )
        return CheckBoxEditHolder(binding)
    }

    inner class CheckBoxEditHolder(val binding: RecyclerItemTodoFirstNoteEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            binding.text = firstNote
            _checkBoxHolder.value = this
            binding.executePendingBindings()
        }
    }
}