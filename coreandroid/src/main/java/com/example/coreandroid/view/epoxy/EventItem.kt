package com.example.coreandroid.view.epoxy

import android.view.View
import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.example.coreandroid.EventInfoBindingModel_
import com.example.coreandroid.EventThumbnailBindingModel_
import com.example.coreandroid.R

class EventItem(
    private val clicked: View.OnClickListener,
    thumbnail: EventThumbnailBindingModel_,
    info: EventInfoBindingModel_,
    kindsCarousel: CarouselModel_
) : EpoxyModelGroup(R.layout.event_item, thumbnail, info, kindsCarousel) {
    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        holder.rootView.setOnClickListener(clicked)
    }
}