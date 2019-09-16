package com.example.coreandroid.view.binding

import androidx.databinding.BindingAdapter
import ss.com.bannerslider.Slider
import ss.com.bannerslider.adapters.SliderAdapter

@BindingAdapter("sliderAdapter")
fun bindSliderBannerAdapter(slider: Slider, adapter: SliderAdapter?) {
    slider.setAdapter(adapter)
}