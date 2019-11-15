package com.example.coreandroid.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend inline fun <T, R> T.callSuspending(
    crossinline method: T.() -> R
): R = suspendCoroutine { continuation ->
    try {
        val result = method()
        continuation.resume(result)
    } catch (e: Exception) {
        continuation.resumeWithException(e)
    }
}

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