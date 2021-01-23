package com.example.noteexample.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteexample.R
import com.example.noteexample.database.Image
import com.example.noteexample.databinding.FragmentPagerItemBinding
import com.example.noteexample.utils.GlideApp


class ViewPagerAdapter :
    ListAdapter<Image, ViewPagerAdapter.ImageViewHolder>(ImageDiffCallback()) {

    /**
     * [holder] gets instance of InsertUpdateViewHolder and observed in
     * [com.example.noteexample.ui.AllNotesFragment] to set clickListeners for recycler items
     */

    private val _holder = MutableLiveData<ImageViewHolder>()
    val holder: LiveData<ImageViewHolder> = _holder

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding: FragmentPagerItemBinding = DataBindingUtil
            .inflate(layoutInflater, R.layout.fragment_pager_item, parent, false)
        return ImageViewHolder(binding)
    }

    inner class ImageViewHolder(val binding: FragmentPagerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Image) {
            _holder.value = this
            GlideApp.with(binding.imgOnePhoto.context)
                .load(image.photoPath)
                .into(binding.imgOnePhoto)
            binding.executePendingBindings()
        }
    }
}

