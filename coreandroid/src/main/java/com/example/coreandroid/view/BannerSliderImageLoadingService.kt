package com.example.coreandroid.view

import android.widget.ImageView
import com.bumptech.glide.Glide
import ss.com.bannerslider.ImageLoadingService

object BannerSliderImageLoadingService : ImageLoadingService {
    override fun loadImage(url: String, imageView: ImageView) {
        Glide.with(imageView).load(url).into(imageView)
    }

    override fun loadImage(resource: Int, imageView: ImageView) {
        Glide.with(imageView).load(resource).into(imageView)
    }

    override fun loadImage(url: String, placeHolder: Int, errorDrawable: Int, imageView: ImageView) {
        Glide.with(imageView).load(url).placeholder(placeHolder).error(errorDrawable).into(imageView)
    }
}
