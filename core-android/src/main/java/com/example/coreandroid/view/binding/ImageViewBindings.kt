package com.example.coreandroid.view.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.coreandroid.R
import com.example.coreandroid.model.event.Event

val eventRequestOptions by lazy {
    RequestOptions()
        .placeholder(R.drawable.event_placeholder)
        .centerCrop()
}

@BindingAdapter("event")
fun bindEvent(imageView: ImageView, event: Event) {
    Glide.with(imageView)
        .load(event.imageUrl)
        .apply(eventRequestOptions)
        .into(imageView)
}