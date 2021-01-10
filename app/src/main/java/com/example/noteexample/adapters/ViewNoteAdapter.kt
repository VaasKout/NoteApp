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
private const val ITEM_VIEW_TYPE_FIRST_NOTE = 1
private const val ITEM_VIEW_TYPE_IMAGE = 2

class ViewSimpleNoteAdapter :
    ListAdapter<NoteWithImagesRecyclerItems, RecyclerView.ViewHolder>(NoteWithImagesDiffCallback()) {

    /**
     * ListAdapter for [com.example.noteexample.ui.OneNoteFragment]
     * similar with [com.example.noteexample.adapters.EditSimpleNoteAdapter]
     */

    private val _headerHolder = MutableLiveData<HeaderViewHolder>()
    val headerHolder: LiveData<HeaderViewHolder> = _headerHolder

    private val _imgHolder = MutableLiveData<ImageViewHolder>()
    val imgHolder: LiveData<ImageViewHolder> = _imgHolder

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_VIEW_TYPE_HEADER
            1 -> {
                ITEM_VIEW_TYPE_FIRST_NOTE
            }
            else -> ITEM_VIEW_TYPE_IMAGE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                getItem(position).header?.let { holder.bind(it) }
            }
            is FirstNoteViewHolder -> {
                getItem(position).firstNote?.let { holder.bind(it) }
            }
            is ImageViewHolder -> {
                getItem(position).image?.let { holder.bind(it) }
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

            ITEM_VIEW_TYPE_FIRST_NOTE -> {
                val binding: RecyclerFirstNoteBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.recycler_first_note,
                        parent,
                        false
                    )
                return FirstNoteViewHolder(binding)
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

    inner class FirstNoteViewHolder(val binding: RecyclerFirstNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(firstNote: FirstNote) {
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
}