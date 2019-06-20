package com.example.coreandroid.util

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//TODO: TEST this
//run this inside like a double try catch (catching FlickrException if needed and regular Exception)
//maybe also make a retry mechanism for it similar to the one in RetrofitExt??
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
