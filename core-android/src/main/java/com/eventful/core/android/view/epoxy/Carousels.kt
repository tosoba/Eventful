package com.eventful.core.android.view.epoxy

import android.view.ViewGroup
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.CarouselModel_
import com.eventful.core.android.EventKindBindingModel_
import com.eventful.core.android.model.event.Event

open class NestedScrollingCarouselModel : CarouselModel_() {
    override fun buildView(parent: ViewGroup): Carousel =
        super.buildView(parent).apply { isNestedScrollingEnabled = false }
}

val Event.kindsCarousel: CarouselModel_
    get() =
        NestedScrollingCarouselModel()
            .id("${id}c")
            .models(
                kinds.mapIndexed { index: Int, kind: String ->
                    EventKindBindingModel_().id("${id}k$index").kind(kind)
                })
