package com.example.coreandroid.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

private class AbortFlowException : Exception()

fun <T> Flow<T>.takeWhileInclusive(predicate: suspend (T) -> Boolean): Flow<T> = flow {
    try {
        collect { value ->
            emit(value)
            if (!predicate(value)) throw AbortFlowException()
        }
    } catch (e: AbortFlowException) {
    }
}