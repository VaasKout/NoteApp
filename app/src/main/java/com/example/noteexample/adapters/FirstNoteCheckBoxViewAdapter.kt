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
import com.example.noteexample.databinding.RecyclerItemTodoFirstNoteViewBinding
import javax.inject.Inject

class FirstNoteCheckBoxViewAdapter @Inject constructor():
    ListAdapter<FirstNote, FirstNoteCheckBoxViewAdapter.CheckBoxViewHolder>(FirstNoteDiffCallback()) {

    private val _checkBoxHolder = MutableLiveData<CheckBoxViewHolder>()
    val checkBoxHolder: LiveData<CheckBoxViewHolder> = _checkBoxHolder

    override fun onBindViewHolder(holder: CheckBoxViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckBoxViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding: RecyclerItemTodoFirstNoteViewBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.recycler_item_todo_first_note_view,
                parent,
                false
            )
        return CheckBoxViewHolder(binding)
    }

    inner class CheckBoxViewHolder(val binding: RecyclerItemTodoFirstNoteViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            _checkBoxHolder.value = this
            binding.text = firstNote
            binding.executePendingBindings()
        }
    }
}