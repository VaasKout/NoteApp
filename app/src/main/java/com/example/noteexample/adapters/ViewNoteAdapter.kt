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

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_TODO_FIRST_NOTE = 1
private const val ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE = 2
private const val ITEM_VIEW_TYPE_IMAGE = 3
private const val ITEM_VIEW_TYPE_DATE = 4

class ViewNoteAdapter :
    ListAdapter<NoteWithImagesRecyclerItems, RecyclerView.ViewHolder>(NoteWithImagesDiffCallback()) {

    /**
     * ListAdapter for [com.example.noteexample.ui.OneNoteFragment]
     * similar with [com.example.noteexample.adapters.EditNoteAdapter]
     */
    private var todoList = false

    private val _headerHolder = MutableLiveData<HeaderViewHolder>()
    val headerHolder: LiveData<HeaderViewHolder> = _headerHolder

    private val _firstNoteTodoHolder = MutableLiveData<FirstNoteTodoViewHolder>()
    val firstNoteTodoHolder: LiveData<FirstNoteTodoViewHolder> = _firstNoteTodoHolder

    private val _firstNoteSimpleHolder = MutableLiveData<FirstNoteSimpleViewHolder>()
    val firstNoteSimpleHolder: LiveData<FirstNoteSimpleViewHolder> = _firstNoteSimpleHolder

    private val _imgHolder = MutableLiveData<ImageViewHolder>()
    val imgHolder: LiveData<ImageViewHolder> = _imgHolder

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
                getItem(position).image?.let {
                    return ITEM_VIEW_TYPE_IMAGE
                }
                return ITEM_VIEW_TYPE_DATE
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                getItem(position).header?.let { holder.bind(it) }
            }
            is FirstNoteTodoViewHolder -> {
                getItem(position).firstNote?.let { holder.bind(it) }
            }
            is FirstNoteSimpleViewHolder -> {
                getItem(position).firstNote?.let { holder.bind(it) }
            }
            is ImageViewHolder -> {
                getItem(position).image?.let { holder.bind(it) }
            }
            is DateViewHolder -> {
                getItem(position).date?.let { holder.bind(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> {
                val binding: RecyclerItemHeaderViewBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_item_header_view,
                        parent,
                        false
                    )
                return HeaderViewHolder(binding)
            }

            ITEM_VIEW_TYPE_TODO_FIRST_NOTE -> {
                val binding: RecyclerItemSimpleFirstNoteViewBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_item_simple_first_note_view,
                        parent,
                        false
                    )
                return FirstNoteSimpleViewHolder(binding)
            }

            ITEM_VIEW_TYPE_SIMPLE_FIRST_NOTE -> {
                val binding: RecyclerItemTodoFirstNoteViewBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_item_todo_first_note_view,
                        parent,
                        false
                    )
                return FirstNoteTodoViewHolder(binding)
            }

            ITEM_VIEW_TYPE_IMAGE -> {
                val binding: RecyclerItemImageViewBinding =
                    DataBindingUtil
                        .inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.recycler_item_image_view,
                            parent,
                            false
                        )
                return ImageViewHolder(binding)
            }

            ITEM_VIEW_TYPE_DATE -> {
                val binding: RecyclerItemDateBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_item_date,
                        parent,
                        false
                    )
                return DateViewHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    inner class HeaderViewHolder(val binding: RecyclerItemHeaderViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: Header) {
            binding.header = header
            if (header.title.isNotEmpty()) {
                binding.headerView.visibility = View.VISIBLE
            }
            binding.executePendingBindings()
        }
    }

    inner class FirstNoteTodoViewHolder(val binding: RecyclerItemTodoFirstNoteViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            binding.firstNote = firstNote
            binding.executePendingBindings()
        }
    }

    inner class FirstNoteSimpleViewHolder(val binding: RecyclerItemSimpleFirstNoteViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
            binding.firstNote = firstNote
            binding.executePendingBindings()
        }
    }

    inner class ImageViewHolder(val binding: RecyclerItemImageViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            binding.data = image
            if (image.signature.isNotEmpty()) {
                binding.viewNoteItem.visibility = View.VISIBLE
            }
            _imgHolder.value = this
            binding.executePendingBindings()
        }
    }

    inner class DateViewHolder(val binding: RecyclerItemDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.dateMain.text = date
        }
    }
}