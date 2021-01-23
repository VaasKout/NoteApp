package com.example.noteexample.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.FirstNote
import com.example.noteexample.databinding.RecyclerItemSimpleFirstNoteViewBinding
import com.example.noteexample.databinding.RecyclerItemTodoFirstNoteViewBinding
import com.google.android.material.checkbox.MaterialCheckBox

private const val ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE = 0
private const val ITEM_VIEW_TYPE_TODO_FIRST_NOTE = 1

class FirstNoteAdapter(val pagerStyle: Boolean) :
    ListAdapter<FirstNote, RecyclerView.ViewHolder>(FirstNoteDiffCallback()) {

    private val _firstNoteTodoHolder = MutableLiveData<FirstNoteTodoViewHolder>()
    val firstNoteTodoHolder: LiveData<FirstNoteTodoViewHolder> = _firstNoteTodoHolder

    inner class FirstNoteSimpleViewHolder(val binding: RecyclerItemSimpleFirstNoteViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            binding.firstNote = firstNote
            binding.pagerStyle = pagerStyle
            binding.firstNoteView.isClickable = false
        }
    }

    inner class FirstNoteTodoViewHolder(val binding: RecyclerItemTodoFirstNoteViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            binding.checkboxView.isClickable = pagerStyle
            binding.pagerStyle = pagerStyle
            binding.checkboxView.isChecked = firstNote.isChecked
            _firstNoteTodoHolder.value = this
            binding.firstNote = firstNote
            binding.executePendingBindings()
            crossText(firstNote, binding.checkboxView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_TODO_FIRST_NOTE -> {
                val binding: RecyclerItemTodoFirstNoteViewBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_item_todo_first_note_view,
                        parent,
                        false
                    )
                return FirstNoteTodoViewHolder(binding)
            }
            ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE -> {
                val binding: RecyclerItemSimpleFirstNoteViewBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_item_simple_first_note_view,
                        parent,
                        false
                    )
                return FirstNoteSimpleViewHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FirstNoteTodoViewHolder -> {
                holder.bind(getItem(position))
            }
            is FirstNoteSimpleViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).todoItem) {
            ITEM_VIEW_TYPE_TODO_FIRST_NOTE
        } else {
            ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE
        }
    }

    private fun crossText(firstNote: FirstNote, edit: MaterialCheckBox) {
        if (firstNote.isChecked) {
            edit.paintFlags = edit.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            edit.paintFlags = edit.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
}