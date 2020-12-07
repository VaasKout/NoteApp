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
import com.example.noteexample.databinding.HeaderEditBinding
import com.example.noteexample.databinding.RecyclerImageEditItemBinding

/**
 * ListAdapter for [com.example.noteexample.ui.EditNoteFragment] with header on 0 position
 *
 * [OneNoteEditAdapter.headerHolder] LiveData for header
 * [OneNoteEditAdapter.imgHolder] LiveData for other items
 */

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class OneNoteEditAdapter :
    ListAdapter<NoteWithImagesRecyclerItems, RecyclerView.ViewHolder>(DataDiffCallBack()) {

    private val _headerHolder = MutableLiveData<NoteEditHolder>()
    val headerHolder: LiveData<NoteEditHolder> = _headerHolder

    private val _imgHolder = MutableLiveData<NoteContentEditHolder>()
    val imgHolder: LiveData<NoteContentEditHolder> = _imgHolder

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
        return if (position == 0) {
            ITEM_VIEW_TYPE_HEADER
        } else {
            ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NoteEditHolder -> {
                getItem(position).header?.let { holder.bind(it) }
            }
            is NoteContentEditHolder -> {
                getItem(position).image?.let { holder.bind(it) }
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
                val binding: RecyclerImageEditItemBinding =
                    DataBindingUtil
                        .inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.recycler_image_edit_item,
                            parent,
                            false
                        )
                return NoteContentEditHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    inner class NoteContentEditHolder(val binding: RecyclerImageEditItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(images: Image) {
            _imgHolder.value = this
            binding.data = images
            if (images.photoPath.isEmpty()) {
                binding.deleteCircle.visibility = View.GONE
                binding.deleteCircleIcon.visibility = View.GONE
            }
        }
    }

    inner class NoteEditHolder(val binding: HeaderEditBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: Header) {
            _headerHolder.value = this
            binding.header = header
        }
    }
}
