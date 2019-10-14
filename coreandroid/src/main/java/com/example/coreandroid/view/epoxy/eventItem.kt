package com.example.coreandroid.view.epoxy

import android.view.View
import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.example.coreandroid.*
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.NestedScrollingCarouselModel

open class EventItem(
    private val clicked: View.OnClickListener,
    background: EventSelectableBackgroundBindingModel_,
    thumbnail: EventThumbnailBindingModel_,
    info: EventInfoBindingModel_,
    kindsCarousel: CarouselModel_
) : EpoxyModelGroup(R.layout.event_item, background, thumbnail, info, kindsCarousel) {

    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        holder.rootView.setOnClickListener(clicked)
    }
}

fun Event.listItem(clicked: View.OnClickListener) = EventItem(
    clicked,
    EventSelectableBackgroundBindingModel_().id("${id}b")
        .selected(false),
    EventThumbnailBindingModel_().id("${id}t")
        .event(this),
    EventInfoBindingModel_().id("${id}i")
        .event(this),
    NestedScrollingCarouselModel()
        .id("${id}c")
        .models(kinds.mapIndexed { index: Int, kind: String ->
            EventKindBindingModel_().id("${id}k$index")
                .kind(kind)
        })
)

class SelectableEventItem(
    clicked: View.OnClickListener,
    private val longClicked: View.OnLongClickListener,
    background: EventSelectableBackgroundBindingModel_,
    thumbnail: EventThumbnailBindingModel_,
    info: EventInfoBindingModel_,
    kindsCarousel: CarouselModel_
) : EventItem(clicked, background, thumbnail, info, kindsCarousel) {

    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        holder.rootView.setOnLongClickListener(longClicked)
    }
}

fun Selectable<Event>.listItem(
    selected: Boolean,
    clicked: View.OnClickListener,
    longClicked: View.OnLongClickListener
) = SelectableEventItem(
    clicked,
    longClicked,
    EventSelectableBackgroundBindingModel_().id("${item.id}b")
        .selected(selected),
    EventThumbnailBindingModel_().id("${item.id}t")
        .event(item),
    EventInfoBindingModel_().id("${item.id}i")
        .event(item),
    NestedScrollingCarouselModel()
        .id("${item.id}c")
        .models(item.kinds.mapIndexed { index: Int, kind: String ->
            EventKindBindingModel_().id("${item.id}k$index")
                .kind(kind)
        })
)