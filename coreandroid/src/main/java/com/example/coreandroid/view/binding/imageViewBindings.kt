package com.example.coreandroid.view.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.coreandroid.R
import com.example.coreandroid.ticketmaster.Event

private val eventRequestOptions =
    RequestOptions().placeholder(R.drawable.event_placeholder).centerCrop()

@BindingAdapter("event")
fun bindEvent(imageView: ImageView, event: Event) {
    Glide.with(imageView)
        .load(event.imageUrl)
        .apply(eventRequestOptions)
        .into(imageView)
}