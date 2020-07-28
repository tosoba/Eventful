package com.example.coreandroid.view.epoxy

import android.view.View
import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.example.core.model.Selectable
import com.example.coreandroid.EventInfoBindingModel_
import com.example.coreandroid.EventThumbnailBindingModel_
import com.example.coreandroid.R
import com.example.coreandroid.SelectableBackgroundBindingModel_
import com.example.coreandroid.model.event.Event

class SelectableEventItem(
    private val clicked: View.OnClickListener,
    private val longClicked: View.OnLongClickListener,
    background: SelectableBackgroundBindingModel_,
    thumbnail: EventThumbnailBindingModel_,
    info: EventInfoBindingModel_,
    kindsCarousel: CarouselModel_
) : EpoxyModelGroup(R.layout.event_item, background, thumbnail, info, kindsCarousel) {

    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        with(holder.rootView) {
            setOnClickListener(clicked)
            setOnLongClickListener(longClicked)
        }
    }
}

fun Selectable<Event>.listItem(
    clicked: View.OnClickListener,
    longClicked: View.OnLongClickListener
) = SelectableEventItem(
    clicked,
    longClicked,
    SelectableBackgroundBindingModel_().id("${item.id}eb")
        .selected(selected),
    EventThumbnailBindingModel_().id("${item.id}t")
        .event(item),
    EventInfoBindingModel_().id("${item.id}i")
        .event(item),
    item.kindsCarousel
)
