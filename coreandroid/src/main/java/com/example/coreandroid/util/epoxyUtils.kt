package com.example.coreandroid.util

import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.*
import com.example.coreandroid.loadingIndicator
import com.example.coreandroid.loadingMoreIndicator
import com.example.coreandroid.noItemsText
import com.example.coreandroid.reloadControl
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener

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
    private val visibleThreshold: Int = 5,
    private val minItemsBeforeLoadingMore: Int = 10,
    private val onLoadMore: () -> Unit
) : NestedScrollingCarouselModel() {
    override fun buildView(parent: ViewGroup): Carousel = super.buildView(parent).apply {
        addOnScrollListener(
            EndlessRecyclerViewScrollListener(visibleThreshold, minItemsBeforeLoadingMore) {
                this@InfiniteNestedScrollingCarouselModel.onLoadMore()
            }
        )
    }
}

inline fun EpoxyController.carousel(modelInitializer: CarouselModelBuilder.() -> Unit) {
    NestedScrollingCarouselModel()
        .apply(modelInitializer)
        .addTo(this)
}

inline fun EpoxyController.infiniteCarousel(
    visibleThreshold: Int = 5,
    minItemsBeforeLoadingMore: Int = 0,
    noinline onLoadMore: () -> Unit,
    modelInitializer: CarouselModelBuilder.() -> Unit
) {
    InfiniteNestedScrollingCarouselModel(
        visibleThreshold, minItemsBeforeLoadingMore, onLoadMore
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

fun <I> Fragment.itemListController(
    epoxyThreads: EpoxyThreads,
    reloadClicked: (() -> Unit)? = null,
    onScrollListener: RecyclerView.OnScrollListener? = null,
    showLoadingIndicator: Boolean = true,
    emptyText: String? = null,
    buildItem: (I) -> EpoxyModel<*>
): TypedEpoxyController<HoldsData<List<I>>> = object : TypedEpoxyController<HoldsData<List<I>>>(
    epoxyThreads.builder, epoxyThreads.differ
) {
    override fun buildModels(data: HoldsData<List<I>>) {
        if (view == null || isRemoving) return

        if (data.data.isEmpty()) when (val status = data.status) {
            is Loading -> if (showLoadingIndicator) loadingIndicator {
                id("loading-indicator-items")
            }
            is LoadedSuccessfully -> {
                if (emptyText != null && emptyText.isNotBlank()) noItemsText {
                    id("empty-text")
                    text(emptyText)
                }
            }
            is LoadingFailed<*> -> reloadControl {
                id("reload-control")
                reloadClicked?.let { onReloadClicked(View.OnClickListener { it() }) }
                (status.error as? HasFailureMessage)?.let { message(it.message) }
            }
        } else {
            data.data.forEach {
                buildItem(it).spanSizeOverride { _, _, _ -> 1 }.addTo(this)
            }
            if (data.status is Loading && showLoadingIndicator) loadingMoreIndicator {
                id("loading-indicator-more-items")
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        onScrollListener?.let { recyclerView.addOnScrollListener(it) }
    }
}
