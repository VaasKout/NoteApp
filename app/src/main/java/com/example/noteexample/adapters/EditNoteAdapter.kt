package com.example.noteexample.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.FirstNote
import com.example.noteexample.database.Header
import com.example.noteexample.database.Image
import com.example.noteexample.databinding.RecyclerSimpleFirstNoteEditBinding
import com.example.noteexample.databinding.RecyclerSimpleHeaderEditBinding
import com.example.noteexample.databinding.RecyclerSimpleImageEditBinding
import com.example.noteexample.databinding.RecyclerTodoFirstNoteEditBinding

/**
 * ListAdapter for [com.example.noteexample.ui.EditNoteFragment] with header on 0 position
 *
 * [EditSimpleNoteAdapter.headerHolder] LiveData for header
 * [EditSimpleNoteAdapter.imgHolder] LiveData for other items
 */

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE = 1
private const val ITEM_VIEW_TYPE_TODO_FIRST_NOTE = 2
private const val ITEM_VIEW_TYPE_IMAGE = 3

class EditSimpleNoteAdapter :
    ListAdapter<NoteWithImagesRecyclerItems, RecyclerView.ViewHolder>(NoteWithImagesDiffCallback()) {

    val checkBoxEditAdapter = CheckBoxEditAdapter()

    private val _headerHolder = MutableLiveData<HeaderEditHolder>()
    val headerHolder: LiveData<HeaderEditHolder> = _headerHolder

    private val _firstNoteSimpleHolder = MutableLiveData<FirstNoteSimpleEditHolder>()
    val firstNoteSimpleHolder: LiveData<FirstNoteSimpleEditHolder> = _firstNoteSimpleHolder

    private val _imgHolder = MutableLiveData<ImageEditHolder>()
    val imgHolder: LiveData<ImageEditHolder> = _imgHolder

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_VIEW_TYPE_HEADER
            1 -> {
                getItem(0).header?.let {
                    if (it.todoList) {
                      return ITEM_VIEW_TYPE_TODO_FIRST_NOTE
                    }
                }
               return ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE
            }
            else -> ITEM_VIEW_TYPE_IMAGE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderEditHolder -> {
                getItem(position).header?.let { holder.bind(it) }
            }
            is FirstNoteSimpleEditHolder -> {
                getItem(position).firstNote?.let {
                    if (it.isNotEmpty()) {
                        holder.bind(it[0])
                    }
                }
            }
            is FirstNoteTodoEditHolder -> {
                getItem(position).firstNote?.let { holder.bind(it) }
            }
            is ImageEditHolder -> {
                getItem(position).image?.let { holder.bind(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> {
                val binding: RecyclerSimpleHeaderEditBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_simple_header_edit,
                        parent,
                        false
                    )
                return HeaderEditHolder(binding)
            }

            ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE -> {
                val binding: RecyclerSimpleFirstNoteEditBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_simple_first_note_edit,
                        parent,
                        false
                    )
                return FirstNoteSimpleEditHolder(binding)
            }

            ITEM_VIEW_TYPE_TODO_FIRST_NOTE -> {
                val binding: RecyclerTodoFirstNoteEditBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_todo_first_note_edit,
                        parent,
                        false
                    )
                return FirstNoteTodoEditHolder(binding)
            }

            ITEM_VIEW_TYPE_IMAGE -> {
                val binding: RecyclerSimpleImageEditBinding =
                    DataBindingUtil
                        .inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.recycler_simple_image_edit,
                            parent,
                            false
                        )
                return ImageEditHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }


    inner class HeaderEditHolder(val binding: RecyclerSimpleHeaderEditBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(header: Header) {
            _headerHolder.value = this
            binding.header = header
            binding.executePendingBindings()
        }
    }

    inner class FirstNoteSimpleEditHolder(val binding: RecyclerSimpleFirstNoteEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            binding.firstNote = firstNote
            _firstNoteSimpleHolder.value = this
            binding.executePendingBindings()
        }
    }

    inner class FirstNoteTodoEditHolder(val binding: RecyclerTodoFirstNoteEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: List<FirstNote>) {
            binding.recyclerHeaderTodoEdit.apply {
                adapter = checkBoxEditAdapter
                setHasFixedSize(true)
            }
            checkBoxEditAdapter.submitList(firstNote)
            binding.executePendingBindings()
        }
    }

    inner class ImageEditHolder(val binding: RecyclerSimpleImageEditBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(images: Image) {
            _imgHolder.value = this
            binding.data = images
            if (images.photoPath.isEmpty()) {
                binding.deleteCircle.visibility = View.GONE
                binding.deleteCircleIcon.visibility = View.GONE
            }
            binding.executePendingBindings()
        }
    }
}


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
