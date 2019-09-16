package com.example.coreandroid.util.ext

import android.os.Bundle
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController

private const val KEY_SAVED_SCROLL_POSITION = "KEY_SAVED_SCROLL_POSITION"

fun RecyclerView.saveScrollPosition(outState: Bundle) {
    layoutManager?.onSaveInstanceState()
        ?.let { outState.putParcelable(KEY_SAVED_SCROLL_POSITION, it) }
}

fun RecyclerView.restoreScrollPosition(
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