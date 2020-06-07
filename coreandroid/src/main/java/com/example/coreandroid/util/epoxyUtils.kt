package com.example.coreandroid.util

import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.*
import com.example.core.util.*
import com.example.coreandroid.LoadingMoreIndicatorBindingModel_
import com.example.coreandroid.loadingIndicator
import com.example.coreandroid.noItemsText
import com.example.coreandroid.reloadControl

class EpoxyThreads(val builder: Handler, val differ: Handler)

fun Fragment.simpleController(
    build: EpoxyController.() -> Unit
): AsyncEpoxyController = object : AsyncEpoxyController() {
    override fun buildModels() {
        if (view == null || isRemoving) return
        build()
    }
}

fun <S> Fragment.asyncController(
    epoxyThreads: EpoxyThreads,
    build: EpoxyController.(state: S) -> Unit
): TypedEpoxyController<S> = object : TypedEpoxyController<S>(
    epoxyThreads.builder, epoxyThreads.differ
) {
    override fun buildModels(data: S) {
        if (view == null || isRemoving) return
        build(data)
    }
}

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
    InfiniteNestedScrollingCarouselModel(loadMore).apply(modelInitializer).addTo(this)
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

fun <I> Fragment.infiniteItemListController(
    epoxyThreads: EpoxyThreads,
    reloadClicked: (() -> Unit)? = null,
    emptyText: String? = null,
    loadMore: () -> Unit,
    buildItem: (I) -> EpoxyModel<*>
): TypedEpoxyController<HoldsList<I>> = object : TypedEpoxyController<HoldsList<I>>(
    epoxyThreads.builder, epoxyThreads.differ
) {
    override fun buildModels(data: HoldsList<I>) {
        if (view == null || isRemoving) return

        if (data.data.isEmpty()) when (val status = data.status) {
            is Loading -> loadingIndicator { id("loading-indicator-items") }
            is LoadedSuccessfully -> if (emptyText != null && emptyText.isNotBlank()) noItemsText {
                id("empty-text")
                text(emptyText)
            }
            is Failure -> reloadControl {
                id("reload-control")
                reloadClicked?.let { onReloadClicked(View.OnClickListener { it() }) }
                (status.error as? HasFailureMessage)?.let { message(it.message) }
            }
        } else {
            data.data.forEach {
                buildItem(it).spanSizeOverride { _, _, _ -> 1 }.addTo(this)
            }
            LoadingMoreIndicatorBindingModel_()
                .id("loading-indicator-more-items")
                .onBind { _, _, _ -> loadMore() }
                .addIf(data.canLoadMore, this)
        }
    }
}
