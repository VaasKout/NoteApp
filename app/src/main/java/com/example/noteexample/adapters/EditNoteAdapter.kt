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
import com.example.noteexample.databinding.*

/**
 * ListAdapter for [com.example.noteexample.ui.EditNoteFragment] with header on 0 position
 *
 * [EditNoteAdapter.headerHolder] LiveData for header
 * [EditNoteAdapter.imgHolder] LiveData for other items
 */

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE = 1
private const val ITEM_VIEW_TYPE_TODO_FIRST_NOTE = 2
private const val ITEM_VIEW_TYPE_IMAGE = 3

class EditNoteAdapter :
    ListAdapter<NoteWithImagesRecyclerItems, RecyclerView.ViewHolder>(NoteWithImagesDiffCallback()) {

    private var todoList = false

    private val _headerHolder = MutableLiveData<HeaderEditHolder>()
    val headerHolder: LiveData<HeaderEditHolder> = _headerHolder

    private val _firstNoteSimpleHolder = MutableLiveData<FirstNoteSimpleEditHolder>()
    val firstNoteSimpleHolder: LiveData<FirstNoteSimpleEditHolder> = _firstNoteSimpleHolder

    private val _firstNoteTodoHolder = MutableLiveData<FirstNoteTodoEditHolder>()
    val firstNoteTodoHolder: LiveData<FirstNoteTodoEditHolder> = _firstNoteTodoHolder

    private val _imgHolder = MutableLiveData<ImageEditHolder>()
    val imgHolder: LiveData<ImageEditHolder> = _imgHolder

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0 -> {
                getItem(position).header?.let {
                    todoList = it.todoList
                }
                return ITEM_VIEW_TYPE_HEADER
            }
            else -> {
                getItem(position).firstNote?.let {
                    return if (!todoList) {
                        ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE
                    } else {
                        ITEM_VIEW_TYPE_TODO_FIRST_NOTE
                    }
                }
                return ITEM_VIEW_TYPE_IMAGE
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderEditHolder -> {
                getItem(position).header?.let { holder.bind(it) }
            }
            is FirstNoteSimpleEditHolder -> {
                getItem(position).firstNote?.let { holder.bind(it) }
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
                val binding: RecyclerItemHeaderEditBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_item_header_edit,
                        parent,
                        false
                    )
                return HeaderEditHolder(binding)
            }

            ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE -> {
                val binding: RecyclerItemSimpleFirstNoteEditBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_item_simple_first_note_edit,
                        parent,
                        false
                    )
                return FirstNoteSimpleEditHolder(binding)
            }

            ITEM_VIEW_TYPE_TODO_FIRST_NOTE -> {
                val binding: RecyclerItemTodoFirstNoteEditBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_item_todo_first_note_edit,
                        parent,
                        false
                    )
                return FirstNoteTodoEditHolder(binding)
            }

            ITEM_VIEW_TYPE_IMAGE -> {
                val binding: RecyclerItemImageEditBinding =
                    DataBindingUtil
                        .inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.recycler_item_image_edit,
                            parent,
                            false
                        )
                return ImageEditHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }


    inner class HeaderEditHolder(val binding: RecyclerItemHeaderEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: Header) {
            _headerHolder.value = this
            binding.header = header
            binding.executePendingBindings()
        }
    }

    inner class FirstNoteSimpleEditHolder(val binding: RecyclerItemSimpleFirstNoteEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            _firstNoteSimpleHolder.value = this
            binding.firstNote = firstNote
            binding.executePendingBindings()
        }
    }

    inner class FirstNoteTodoEditHolder(val binding: RecyclerItemTodoFirstNoteEditBinding):
    RecyclerView.ViewHolder(binding.root){
        fun bind(firstNote: FirstNote){
            _firstNoteTodoHolder.value = this
            binding.firstNote = firstNote
            binding.executePendingBindings()
        }
    }

    inner class ImageEditHolder(val binding: RecyclerItemImageEditBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            _imgHolder.value = this
            binding.data = image
            if (image.photoPath.isEmpty()) {
                binding.deleteCircle.visibility = View.GONE
                binding.deleteCircleIcon.visibility = View.GONE
            }
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
