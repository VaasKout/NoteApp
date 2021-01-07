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
import com.example.noteexample.databinding.RecyclerMainItemBinding
import com.example.noteexample.databinding.RecyclerTodoCheckboxEditBinding

class CheckBoxEditAdapter:
    ListAdapter<FirstNote, CheckBoxEditAdapter.CheckBoxEditHolder>(FirstNoteDiffCallback()) {

    private val _checkBoxHolder = MutableLiveData<CheckBoxEditHolder>()
    val checkBoxHolder: LiveData<CheckBoxEditHolder> = _checkBoxHolder

    override fun onBindViewHolder(holder: CheckBoxEditAdapter.CheckBoxEditHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckBoxEditHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding: RecyclerTodoCheckboxEditBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.recycler_todo_checkbox_edit,
                parent,
                false
            )
        return CheckBoxEditHolder(binding)
    }

    inner class CheckBoxEditHolder(val binding: RecyclerTodoCheckboxEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            _checkBoxHolder.value = this
            binding.text = firstNote
            binding.executePendingBindings()
        }
    }
}