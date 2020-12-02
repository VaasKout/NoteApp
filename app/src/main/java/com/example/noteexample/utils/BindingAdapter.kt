package com.example.noteexample.utils

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.example.noteexample.database.Header
import com.example.noteexample.database.Image

@GlideModule
class GlideAppModule : AppGlideModule()


@BindingAdapter("titleText")
fun TextView.titleText(header: Header?) {
    header?.let {
        when {
            this is EditText && it.title.isNotEmpty() -> {
                setText(it.title)
            }
            this !is EditText && it.title.isNotEmpty() -> {
                visibility = View.VISIBLE
                text = it.title
            }
            this !is EditText && it.text.isEmpty() ->{
                visibility = View.GONE
            }
        }
    }
}


@BindingAdapter("firstNoteText")
fun TextView.firstNoteText(header: Header?) {
    header?.let {
        when {
            this is EditText && it.text.isNotEmpty() -> {
                setText(it.text)
            }
            this !is EditText && it.text.isNotEmpty() -> {
                visibility = View.VISIBLE
                text = it.text
            }
            this !is EditText && it.text.isEmpty() ->{
                visibility = View.GONE
            }
        }
    }
}

@BindingAdapter("photoNoteText")
fun TextView.photoNoteText(data: Image?) {
    data?.let {
        when {
            this is EditText -> {
                setText(data.signature)
            }
            this !is EditText && data.signature.isNotEmpty() -> {
                visibility = View.VISIBLE
                text = data.signature
            }
            this !is EditText && data.signature.isEmpty() ->{
                visibility = View.GONE
            }
        }
    }
}

@BindingAdapter("date")
fun TextView.setDate(header: Header?) {
    header?.let {
        text = it.date
    }
}

@BindingAdapter("imageUrl")
fun ImageView.bindViewImage(imgUrl: String?) {
    if (!imgUrl.isNullOrEmpty()) {
        GlideApp.with(this.context)
            .load(imgUrl)
            .into(this)
    }
}

