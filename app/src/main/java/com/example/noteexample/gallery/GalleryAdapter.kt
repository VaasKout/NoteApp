package com.example.noteexample.gallery

import android.app.Activity
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.noteexample.R
import com.example.noteexample.databinding.GalleryRecyclerItemBinding


class GalleryAdapter(private val activity: Activity):
    androidx.recyclerview.widget.ListAdapter<Bitmap, GalleryAdapter.GalleryViewHolder>(
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
                false
            )

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

    inner class GalleryViewHolder(private val binding: GalleryRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bitmap: Bitmap) {
            _holder.value = this
            Glide.with(activity)
                .load(bitmap)
                .into(binding.galleryImage)
            binding.executePendingBindings()

        }
    }
}

class NoteDiffCallBack : DiffUtil.ItemCallback<Bitmap>() {
    override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
        return oldItem.generationId == newItem.generationId
    }
}