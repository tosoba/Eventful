package com.example.coreandroid.view.epoxy

import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.TypedEpoxyController
import com.example.core.util.*
import com.example.coreandroid.LoadingMoreIndicatorBindingModel_
import com.example.coreandroid.loadingIndicator
import com.example.coreandroid.noItemsText
import com.example.coreandroid.reloadControl

class EpoxyThreads(val builder: Handler, val differ: Handler) {
    enum class Names(val value: String) {
        DIFFING("epoxy-diffing-thread"),
        BUILDING("epoxy-building-thread")
    }
}

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

fun <D, I> Fragment.infiniteItemListController(
    epoxyThreads: EpoxyThreads,
    mapToHoldsList: D.() -> HoldsList<I>,
    reloadClicked: (() -> Unit)? = null,
    emptyTextResource: ((D) -> Int)? = null,
    loadMore: () -> Unit,
    buildItem: (I) -> EpoxyModel<*>
): TypedEpoxyController<D> = object : TypedEpoxyController<D>(
    epoxyThreads.builder, epoxyThreads.differ
) {
    override fun buildModels(data: D) {
        if (view == null || isRemoving) return

        val holder = data.mapToHoldsList()
        if (holder.data.isEmpty()) when (val status = holder.status) {
            is Loading -> loadingIndicator { id("loading-indicator-items") }
            is LoadedSuccessfully -> if (emptyTextResource != null) {
                val emptyText = context?.getText(emptyTextResource(data))?.toString()
                if (emptyText != null) noItemsText {
                    id("empty-text")
                    text(emptyText)
                }
            }
            is Failure -> reloadControl {
                id("reload-control")
                reloadClicked?.let { onReloadClicked(View.OnClickListener { it() }) }
                (status.error as? HasFailureMessage)?.let { message(it.message) }
            }
        } else {
            holder.data.forEach {
                buildItem(it).spanSizeOverride { _, _, _ -> 1 }.addTo(this)
            }
            LoadingMoreIndicatorBindingModel_()
                .id("loading-indicator-more-items")
                .onBind { _, _, _ -> loadMore() }
                .addIf(holder.canLoadMore, this)
        }
    }
}
