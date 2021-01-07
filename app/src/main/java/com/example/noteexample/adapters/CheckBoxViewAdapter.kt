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
import com.example.noteexample.databinding.RecyclerTodoCheckboxViewBinding

class CheckBoxViewAdapter :
    ListAdapter<FirstNote, CheckBoxViewAdapter.CheckBoxViewHolder>(FirstNoteDiffCallback()) {

    private val _checkBoxHolder = MutableLiveData<CheckBoxViewHolder>()
    val checkBoxHolder: LiveData<CheckBoxViewHolder> = _checkBoxHolder

    override fun onBindViewHolder(holder: CheckBoxViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckBoxViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding: RecyclerTodoCheckboxViewBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.recycler_todo_checkbox_edit,
                parent,
                false
            )
        return CheckBoxViewHolder(binding)
    }

    inner class CheckBoxViewHolder(val binding: RecyclerTodoCheckboxViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            _checkBoxHolder.value = this
            binding.text = firstNote
            binding.executePendingBindings()
        }
    }
}