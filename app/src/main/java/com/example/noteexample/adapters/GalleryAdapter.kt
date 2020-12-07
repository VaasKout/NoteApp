package com.example.noteexample.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.databinding.GalleryRecyclerItemBinding

/**
 * Data class for image items
 * [GalleryData.isChecked] is used to select photos are going to be inserted
 */
data class GalleryData (
    val imgSrcUrl: String,
    var isChecked: Boolean = false,
)

class GalleryAdapter :
    ListAdapter<GalleryData, GalleryAdapter.GalleryViewHolder>(
        GalleryDiffCallBack()
    ) {
    /**
     * [holder] LiveData observes each item for [com.example.noteexample.ui.GalleryFragment]
     * to setOnClickListener for each image
     */
    private val _holder = MutableLiveData<GalleryViewHolder>()
    val holder: LiveData<GalleryViewHolder> = _holder

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        /**
         * Use only DataBindingUtil else layout options crash in recycler_main_item
         */
        val binding: GalleryRecyclerItemBinding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.gallery_recycler_item,
                parent,
                false
            )

        return GalleryViewHolder(binding)
    }

    inner class GalleryViewHolder(val binding: GalleryRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /**
         * Checked state of card depends on [GalleryData.isChecked] state
         */
        fun bind(data: GalleryData) {
            _holder.value = this
            binding.data = data
            binding.galleryCard.isChecked = data.isChecked
            binding.executePendingBindings()
        }
    }
}

class GalleryDiffCallBack : DiffUtil.ItemCallback<GalleryData>() {
    override fun areItemsTheSame(oldItem: GalleryData, newItem: GalleryData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: GalleryData, newItem: GalleryData): Boolean {
        return oldItem == newItem
    }

}