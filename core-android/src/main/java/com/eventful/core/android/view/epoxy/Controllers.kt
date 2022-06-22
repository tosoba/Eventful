package com.eventful.core.android.view.epoxy

import android.os.Handler
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.TypedEpoxyController
import com.eventful.core.android.LoadingMoreIndicatorBindingModel_
import com.eventful.core.android.imageBackgroundInfo
import com.eventful.core.android.loadingIndicator
import com.eventful.core.android.reloadControl
import com.eventful.core.util.*

class EpoxyThreads(val builder: Handler, val differ: Handler) {
    enum class Names(val value: String) {
        DIFFING("epoxy-diffing-thread"),
        BUILDING("epoxy-building-thread")
    }
}

fun Fragment.asyncController(build: EpoxyController.() -> Unit): AsyncEpoxyController =
    object : AsyncEpoxyController() {
        override fun buildModels() {
            if (view == null || isRemoving) return
            build()
        }
    }

fun <D> Fragment.typedController(
    epoxyThreads: EpoxyThreads,
    build: EpoxyController.(D) -> Unit
): TypedEpoxyController<D> =
    object : TypedEpoxyController<D>(epoxyThreads.builder, epoxyThreads.differ) {
        override fun buildModels(data: D) {
            if (view == null || isRemoving) return
            build(data)
        }
    }

fun <D, I> Fragment.infiniteItemListController(
    epoxyThreads: EpoxyThreads,
    mapToHoldsList: D.() -> HoldsList<I>,
    reloadClicked: (() -> Unit)? = null,
    @DrawableRes imageBackgroundResource: Int,
    @StringRes initialDescriptionResource: Int,
    emptyTextResource: ((D) -> Int)? = null,
    loadMore: () -> Unit,
    buildItem: (I) -> EpoxyModel<*>
): TypedEpoxyController<D> =
    typedController(epoxyThreads) { data ->
        val holder = data.mapToHoldsList()
        if (holder.data.isEmpty())
            when (val status = holder.status) {
                is Initial ->
                    imageBackgroundInfo {
                        id("initial-background")
                        imageRes(imageBackgroundResource)
                        description(requireContext().getText(initialDescriptionResource).toString())
                    }
                is Loading -> loadingIndicator { id("loading-indicator-items") }
                is LoadedSuccessfully ->
                    if (emptyTextResource != null)
                        imageBackgroundInfo {
                            id("empty-background")
                            imageRes(imageBackgroundResource)
                            description(
                                requireContext().getText(emptyTextResource(data)).toString())
                        }
                is Failure ->
                    reloadControl {
                        id("reload-control")
                        reloadClicked?.let { onReloadClicked(View.OnClickListener { it() }) }
                        (status.error as? HasFailureMessage)?.let { message(it.message) }
                    }
            }
        else {
            holder.data.forEach { buildItem(it).spanSizeOverride { _, _, _ -> 1 }.addTo(this) }
            LoadingMoreIndicatorBindingModel_()
                .id("loading-indicator-more-items")
                .onBind { _, _, _ -> loadMore() }
                .addIf(holder.canLoadMore, this)
        }
    }
