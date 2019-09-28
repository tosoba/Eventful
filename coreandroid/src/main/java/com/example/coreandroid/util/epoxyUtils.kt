package com.example.coreandroid.util

import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.*
import com.example.coreandroid.*
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import com.haroldadmin.vector.VectorFragment
import com.haroldadmin.vector.VectorState
import com.haroldadmin.vector.VectorViewModel
import com.haroldadmin.vector.withState
import kotlin.reflect.KProperty1


fun VectorFragment.simpleController(
    build: EpoxyController.() -> Unit
) = object : AsyncEpoxyController() {
    override fun buildModels() {
        if (view == null || isRemoving) return
        build()
    }
}

fun <S : VectorState, A : VectorViewModel<S>> VectorFragment.simpleController(
    viewModel: A, buildModels: EpoxyController.(state: S) -> Unit
) = object : AsyncEpoxyController() {
    override fun buildModels() {
        if (view == null || isRemoving) return
        withState(viewModel) { state ->
            buildModels(state)
        }
    }
}

fun <S : VectorState, A : VectorViewModel<S>> VectorFragment.asyncController(
    modelBuildingHandler: Handler, diffingHandler: Handler,
    viewModel: A, build: EpoxyController.(state: S) -> Unit
) = object : TypedEpoxyController<S>(modelBuildingHandler, diffingHandler) {
    override fun buildModels(data: S) {
        if (view == null || isRemoving) return
        withState(viewModel) { state ->
            build(state)
        }
    }
}

fun <A : VectorViewModel<B>, B : VectorState, C : VectorViewModel<D>, D : VectorState> VectorFragment.simpleController(
    viewModel1: A, viewModel2: C,
    buildUsing: EpoxyController.(state1: B, state2: D) -> Unit
) = object : AsyncEpoxyController() {
    override fun buildModels() {
        if (view == null || isRemoving) return
        withState(viewModel1, viewModel2) { state1, state2 ->
            buildUsing(state1, state2)
        }
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
    visibleThreshold: Int = 5, minItemsBeforeLoadingMore: Int = 0,
    noinline onLoadMore: () -> Unit, modelInitializer: CarouselModelBuilder.() -> Unit
) {
    InfiniteNestedScrollingCarouselModel(
        visibleThreshold, minItemsBeforeLoadingMore, onLoadMore
    ).apply(modelInitializer).addTo(this)
}

inline fun <T> CarouselModelBuilder.withModelsFrom(
    items: Collection<T>, modelBuilder: (T) -> EpoxyModel<*>
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
    items: Map<T, R>, modelBuilder: (T, R) -> EpoxyModel<*>
) {
    models(items.map { (key, value) -> modelBuilder(key, value) })
}

fun <S : VectorState, A : VectorViewModel<S>, L : HoldsData<Collection<I>>, I> VectorFragment.itemListController(
    modelBuildingHandler: Handler, diffingHandler: Handler,
    viewModel: A, prop: KProperty1<S, L>,
    reloadClicked: (() -> Unit)? = null,
    onScrollListener: RecyclerView.OnScrollListener? = null,
    showLoadingIndicator: Boolean = true,
    emptyText: String? = null,
    buildItem: (I) -> EpoxyModel<*>
) = object : TypedEpoxyController<S>(modelBuildingHandler, diffingHandler) {

    override fun buildModels(data: S) {
        if (view == null || isRemoving) return

        withState(viewModel) { state ->
            val items = prop.get(state)
            if (items.value.isEmpty()) when (val status = items.status) {
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
                items.value.forEach {
                    buildItem(it).spanSizeOverride { _, _, _ -> 1 }.addTo(this)
                }
                if (items.status is Loading && showLoadingIndicator) loadingMoreIndicator {
                    id("loading-indicator-more-items")
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        onScrollListener?.let { recyclerView.addOnScrollListener(it) }
    }
}

class Column(
    models: Collection<EpoxyModel<*>>,
    private val clicked: View.OnClickListener? = null
) : EpoxyModelGroup(R.layout.column, models) {

    constructor(
        clicked: View.OnClickListener? = null,
        vararg models: EpoxyModel<*>
    ) : this(models.toList(), clicked)

    constructor(
        vararg models: EpoxyModel<*>
    ) : this(models.toList(), null)


    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        clicked?.let { holder.rootView.setOnClickListener(it) }
    }
}

class Row(
    models: Collection<EpoxyModel<*>>,
    private val clicked: View.OnClickListener? = null
) : EpoxyModelGroup(R.layout.row, models) {

    constructor(
        clicked: View.OnClickListener? = null,
        vararg models: EpoxyModel<*>
    ) : this(models.toList(), clicked)

    constructor(
        vararg models: EpoxyModel<*>
    ) : this(models.toList(), null)

    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        clicked?.let { holder.rootView.setOnClickListener(it) }
    }
}
