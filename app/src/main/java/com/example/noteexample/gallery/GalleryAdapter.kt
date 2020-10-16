package com.example.noteexample.gallery

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.GalleryData
import com.example.noteexample.databinding.GalleryRecyclerItemBinding


class GalleryAdapter(private val activity: Activity) :
    ListAdapter<GalleryData, GalleryAdapter.GalleryViewHolder>(
        NoteDiffCallBack()
    ) {


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
                false)

//        val size = Point()
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//            val display = activity.display
//            display?.getRealSize(size)
//            val width: Int = size.x / 3
//            binding.galleryCard.minimumWidth = width
//            binding.galleryCard.minimumHeight = width
//            binding.galleryImage.maxHeight = width
//            binding.galleryImage.maxWidth = width
//
//        } else{
//            val display: Display = activity.windowManager.defaultDisplay
//            display.getRealSize(size)
//            val width: Int = size.x / 3
//            binding.galleryCard.minimumWidth = width
//            binding.galleryCard.minimumHeight = width
//            binding.galleryImage.maxHeight = width
//            binding.galleryImage.maxWidth = width
//        }

        return GalleryViewHolder(binding)
    }

    inner class GalleryViewHolder(val binding: GalleryRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: GalleryData) {
            _holder.value = this
            binding.data = data
            binding.executePendingBindings()
        }
    }
}

class NoteDiffCallBack : DiffUtil.ItemCallback<GalleryData>() {
    override fun areItemsTheSame(oldItem: GalleryData, newItem: GalleryData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: GalleryData, newItem: GalleryData): Boolean {
        return oldItem == newItem
    }

}