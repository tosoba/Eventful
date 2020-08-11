package com.eventful.core.android.util.ext

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

fun <T> Flow<T>.logEach(tag: String, type: LogType): Flow<T> = onEach {
    Log.e(
        tag,
        "${javaClass.simpleName.replace(type.suffix, "")}:${it}"
    )
}

enum class LogType(val suffix: String) {
    VIEW_MODEL("ViewModel"),
    FRAGMENT("Fragment")
}
