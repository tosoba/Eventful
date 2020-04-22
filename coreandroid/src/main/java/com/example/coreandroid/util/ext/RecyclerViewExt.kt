package com.example.coreandroid.util.ext

import android.os.Bundle
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView

private const val KEY_SAVED_SCROLL_POSITION = "KEY_SAVED_SCROLL_POSITION"

fun RecyclerView.saveScrollPosition(outState: Bundle) {
    layoutManager?.onSaveInstanceState()
        ?.let { outState.putParcelable(KEY_SAVED_SCROLL_POSITION, it) }
}

private fun RecyclerView.restoreScrollPosition(
    savedInstanceState: Bundle,
    epoxyController: EpoxyController
) {
    var interceptor: EpoxyController.Interceptor? = null
    interceptor = EpoxyController.Interceptor {
        savedInstanceState.getParcelable<Parcelable>(KEY_SAVED_SCROLL_POSITION)?.let {
            layoutManager?.onRestoreInstanceState(it)
        }
        epoxyController.removeInterceptor(interceptor!!)
    }
    epoxyController.addInterceptor(interceptor)
}

fun EpoxyRecyclerView.onCreateControllerView(
    epoxyController: EpoxyController,
    savedInstanceState: Bundle?
) {
    setController(epoxyController)
    savedInstanceState?.let { restoreScrollPosition(it, epoxyController) }
}