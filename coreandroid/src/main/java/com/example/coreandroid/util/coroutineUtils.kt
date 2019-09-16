package com.example.coreandroid.util

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
