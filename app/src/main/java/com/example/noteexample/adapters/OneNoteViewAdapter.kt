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
import com.example.noteexample.database.Header
import com.example.noteexample.database.Image
import com.example.noteexample.databinding.HeaderViewBinding
import com.example.noteexample.databinding.RecyclerImageViewItemBinding

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class OneNoteViewAdapter :
    ListAdapter<NoteWithImagesRecyclerItems, RecyclerView.ViewHolder>(DataDiffCallBack()) {

    /**
     * ListAdapter for [com.example.noteexample.ui.OneNoteFragment]
     * similar with [com.example.noteexample.adapters.OneNoteEditAdapter]
     */
    private val _noteContentHolder = MutableLiveData<NoteContentViewHolder>()
    val noteContentHolder: LiveData<NoteContentViewHolder> = _noteContentHolder

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            ITEM_VIEW_TYPE_HEADER
        } else {
            ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NoteViewHolder -> {
                getItem(position).header?.let { holder.bind(it) }
            }
            is NoteContentViewHolder -> {
                getItem(position).image?.let { holder.bind(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> {
                val binding: HeaderViewBinding =
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.header_view,
                        parent,
                        false
                    )
                return NoteViewHolder(binding)
            }
            ITEM_VIEW_TYPE_ITEM -> {
                val binding: RecyclerImageViewItemBinding =
                    DataBindingUtil
                        .inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.recycler_image_view_item,
                            parent,
                            false
                        )
                return NoteContentViewHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    inner class NoteContentViewHolder(val binding: RecyclerImageViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            _noteContentHolder.value = this
            binding.data = image
            if (image.signature.isNotEmpty()){
                binding.viewNoteItem.visibility = View.VISIBLE
            }
        }
    }

    inner class NoteViewHolder(val binding: HeaderViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: Header) {
            binding.header = header
            if (header.title.isNotEmpty() && header.text.isNotEmpty()){
                binding.headerView.visibility = View.VISIBLE
            }
        }
    }
}