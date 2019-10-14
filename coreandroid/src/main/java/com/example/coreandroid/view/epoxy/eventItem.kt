package com.example.coreandroid.view.epoxy

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.example.coreandroid.EventInfoBindingModel_
import com.example.coreandroid.EventKindBindingModel_
import com.example.coreandroid.EventThumbnailBindingModel_
import com.example.coreandroid.R
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.NestedScrollingCarouselModel

open class EventItem(
    private val clicked: View.OnClickListener,
    thumbnail: EventThumbnailBindingModel_,
    info: EventInfoBindingModel_,
    kindsCarousel: CarouselModel_
) : EpoxyModelGroup(R.layout.event_item, thumbnail, info, kindsCarousel) {

    override fun buildView(parent: ViewGroup): View = super.buildView(parent).apply {
        setOnClickListener(clicked)
    }
}

fun Event.listItem(clicked: View.OnClickListener) = EventItem(
    clicked,
    EventThumbnailBindingModel_().id("${id}t")
        .event(this),
    EventInfoBindingModel_().id("${id}b")
        .event(this),
    NestedScrollingCarouselModel()
        .id("${id}t")
        .models(kinds.mapIndexed { index: Int, kind: String ->
            EventKindBindingModel_().id("${id}k$index")
                .kind(kind)
        })
)

class SelectableEventItem(
    private val selectableEvent: Selectable<Event>,
    clicked: View.OnClickListener,
    private val longClicked: View.OnLongClickListener,
    thumbnail: EventThumbnailBindingModel_,
    info: EventInfoBindingModel_,
    kindsCarousel: CarouselModel_
) : EventItem(clicked, thumbnail, info, kindsCarousel) {

    override fun buildView(parent: ViewGroup): View = super.buildView(parent).apply {
        setOnLongClickListener(longClicked)
    }

    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        if (selectableEvent.selected) holder.rootView.setBackgroundColor(
            ContextCompat.getColor(holder.rootView.context, R.color.lightGrayText)
        )
    }
}

fun Selectable<Event>.listItem(
    clicked: View.OnClickListener,
    longClicked: View.OnLongClickListener
) = SelectableEventItem(
    this,
    clicked,
    longClicked,
    EventThumbnailBindingModel_().id("${item.id}t")
        .event(item),
    EventInfoBindingModel_().id("${item.id}b")
        .event(item),
    NestedScrollingCarouselModel()
        .id("${item.id}t")
        .models(item.kinds.mapIndexed { index: Int, kind: String ->
            EventKindBindingModel_().id("${item.id}k$index")
                .kind(kind)
        })
)