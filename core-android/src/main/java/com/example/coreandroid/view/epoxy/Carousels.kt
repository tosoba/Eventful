package com.example.coreandroid.view.epoxy

import android.view.ViewGroup
import com.airbnb.epoxy.*

open class NestedScrollingCarouselModel : CarouselModel_() {
    override fun buildView(parent: ViewGroup): Carousel = super.buildView(parent).apply {
        isNestedScrollingEnabled = false
    }
}

class InfiniteNestedScrollingCarouselModel(
    private val loadMore: () -> Unit
) : NestedScrollingCarouselModel() {
    override fun buildView(parent: ViewGroup): Carousel = super.buildView(parent).apply {
        onBind { _, _, _ -> loadMore() }
    }
}

inline fun EpoxyController.carousel(modelInitializer: CarouselModelBuilder.() -> Unit) {
    NestedScrollingCarouselModel()
        .apply(modelInitializer)
        .addTo(this)
}

inline fun EpoxyController.infiniteCarousel(
    noinline loadMore: () -> Unit,
    modelInitializer: CarouselModelBuilder.() -> Unit
) {
    InfiniteNestedScrollingCarouselModel(
        loadMore
    ).apply(modelInitializer).addTo(this)
}

inline fun <T> CarouselModelBuilder.withModelsFrom(
    items: Collection<T>,
    modelBuilder: (T) -> EpoxyModel<*>
) {
    models(items.map { modelBuilder(it) })
}

inline fun <T> CarouselModelBuilder.withModelsFrom(
    items: Collection<T>,
    extraModels: Collection<EpoxyModel<*>>,
    modelBuilder: (T) -> EpoxyModel<*>
) {
    models(items.map { modelBuilder(it) } + extraModels)
}

inline fun <T, R> CarouselModelBuilder.withModelsFrom(
    items: Map<T, R>,
    modelBuilder: (T, R) -> EpoxyModel<*>
) {
    models(items.map { (key, value) -> modelBuilder(key, value) })
}