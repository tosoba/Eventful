package com.eventful.core.android.view.binding

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.eventful.core.android.R
import com.eventful.core.android.model.event.Event

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

@BindingAdapter("resource")
fun bindResource(imageView: ImageView, @DrawableRes resource: Int) {
    imageView.setImageResource(resource)
}
