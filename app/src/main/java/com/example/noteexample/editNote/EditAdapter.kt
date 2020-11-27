package com.example.noteexample.editNote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.Note
import com.example.noteexample.database.NoteContent
import com.example.noteexample.databinding.HeaderEditBinding
import com.example.noteexample.databinding.RecyclerNoteContentEditItemBinding
import com.example.noteexample.utils.DataDiffCallBack
import com.example.noteexample.utils.DataItem

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class OneNoteEditAdapter :
    ListAdapter<DataItem, RecyclerView.ViewHolder>(DataDiffCallBack()) {

    private val _noteHolder = MutableLiveData<NoteEditHolder>()
    val noteHolder: LiveData<NoteEditHolder> = _noteHolder

    private val _noteContentHolder = MutableLiveData<NoteContentEditHolder>()
    val noteContentHolder: LiveData<NoteContentEditHolder> = _noteContentHolder

//    private val adapterScope = CoroutineScope(Dispatchers.Default)
//    fun addHeaderAndSubmitList(note: Note?, noteContent: List<NoteContent>) {
//        adapterScope.launch {
//            val list = mutableListOf<DataItem>()
//            list.add(0, DataItem(note = note))
//            noteContent.forEach {
//                list.add(DataItem(noteContent = it))
//            }
//            withContext(Dispatchers.Main) {
//                submitList(list)
//            }
//        }
//    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).noteContent == null && getItem(position).note != null) {
            ITEM_VIEW_TYPE_HEADER
        } else {
            ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NoteEditHolder -> {
                holder.bind(getItem(position).note)
            }
            is NoteContentEditHolder -> {
                holder.bind(getItem(position).noteContent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> {
                val binding: HeaderEditBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.header_edit,
                        parent,
                        false
                    )
                return NoteEditHolder(binding)
            }
            ITEM_VIEW_TYPE_ITEM -> {
                val binding: RecyclerNoteContentEditItemBinding =
                    DataBindingUtil
                        .inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.recycler_note_content_edit_item,
                            parent,
                            false
                        )
                return NoteContentEditHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    inner class NoteContentEditHolder(val binding: RecyclerNoteContentEditItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(noteContent: NoteContent?) {
            _noteContentHolder.value = this
            binding.data = noteContent
            noteContent?.let {
                if (it.photoPath.isEmpty()) {
                    binding.deleteCircle.visibility = View.GONE
                    binding.deleteCircleIcon.visibility = View.GONE
                }
            }
        }
    }

    inner class NoteEditHolder(val binding: HeaderEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note?) {
            _noteHolder.value = this
            binding.note = note
        }
    }
}