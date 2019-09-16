package com.example.coreandroid.view

import androidx.annotation.DrawableRes
import ss.com.bannerslider.adapters.SliderAdapter
import ss.com.bannerslider.viewholder.ImageSlideViewHolder


class BannerSliderAdapter(
    private val urls: Collection<String>,
    @DrawableRes private val placeHolder: Int,
    @DrawableRes private val error: Int
) : SliderAdapter() {

    override fun getItemCount(): Int = urls.size

    override fun onBindImageSlide(position: Int, imageSlideViewHolder: ImageSlideViewHolder?) {
        imageSlideViewHolder?.bindImageSlide(urls.elementAt(position), placeHolder, error)
    }
}