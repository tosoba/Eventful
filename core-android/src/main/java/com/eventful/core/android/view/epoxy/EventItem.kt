package com.eventful.core.android.view.epoxy

import android.view.View
import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.eventful.core.model.Selectable
import com.eventful.core.android.EventInfoBindingModel_
import com.eventful.core.android.EventThumbnailBindingModel_
import com.eventful.core.android.R
import com.eventful.core.android.SelectableBackgroundBindingModel_
import com.eventful.core.android.model.event.Event

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
