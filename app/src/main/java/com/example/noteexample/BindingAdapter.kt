package com.example.noteexample

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.example.noteexample.database.GalleryData
import com.example.noteexample.database.Note
import com.example.noteexample.gallery.GalleryAdapter


/**
 * BindingAdapter handle with redundant space in MaterialCardView if one of views is empty
 *
 * android:id="@+id/titleRecyclerItem"
 * app:titleTextVisibility="@{note}"
 *
 * android:id="@+id/noteRecyclerItem"
 * app:noteTextVisibility="@{note}"
 *
 * note is instance of Note from database
 */

@GlideModule
class GlideAppModule : AppGlideModule()

@BindingAdapter("titleTextVisibility")
    fun TextView.setTitleVisibility(note: Note?){
    note?.let {
        if (it.title.isEmpty()) visibility = View.GONE
        else text = it.title
    }
}

@BindingAdapter("imageUrl")
    fun ImageView.bindImage(imgUrl: Uri?){
    imgUrl?.let {
        GlideApp.with(this.context)
            .load(it)
            .fitCenter()
            .into(this)
    }
}

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<GalleryData>?){
    val adapter = recyclerView.adapter as GalleryAdapter
    adapter.submitList(data)
}


//@BindingAdapter("noteTextVisibility")
//    fun TextView.setNoteVisibility(note: Note?){
//        note?.let {
//            if (it.note.isEmpty()) visibility = View.GONE
//            else text = it.note
//        }
//    }


//@BindingAdapter("imageUrl")
//fun bindImage(imgView: ImageView, imgUrl: String?){
//    imgUrl?.let {
//       val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
//        Glide.with(imgView.context)
//                .load(imgUri)
//                .apply(RequestOptions()
//                        .placeholder(R.drawable.loading_animation)
//                        .error(R.drawable.ic_broken_image))
//                .into(imgView)
//    }
//}
