package com.eventful.core.android.util.ext

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

fun <T> Flow<T>.onEachLogging(
    tag: String,
    msgPrefix: String,
    action: (suspend (T) -> Unit)? = null
): Flow<T> = onEach {
    Log.e(
        tag,
        "$msgPrefix:$it"
    )
    action?.invoke(it)
}

enum class LogType(val suffix: String) {
    VIEW_MODEL("ViewModel"),
    FRAGMENT("Fragment")
}
