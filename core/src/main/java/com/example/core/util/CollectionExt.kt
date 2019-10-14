package com.example.core.util

fun <T> Collection<T>.replace(
    mapToNewValue: (T) -> T, matcher: (T) -> Boolean
): Collection<T> = map {
    if (matcher(it)) mapToNewValue(it) else it
}