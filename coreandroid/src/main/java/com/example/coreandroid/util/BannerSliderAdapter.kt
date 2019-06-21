package com.example.coreandroid.util

import com.example.coreandroid.R
import ss.com.bannerslider.adapters.SliderAdapter
import ss.com.bannerslider.viewholder.ImageSlideViewHolder

class BannerSliderAdapter(private val urls: Collection<String>) : SliderAdapter() {
    override fun getItemCount(): Int = urls.size

    override fun onBindImageSlide(position: Int, imageSlideViewHolder: ImageSlideViewHolder?) {
        imageSlideViewHolder?.bindImageSlide(
            urls.elementAt(position),
            R.drawable.event_placeholder,
            R.drawable.event_placeholder
        )
    }
}