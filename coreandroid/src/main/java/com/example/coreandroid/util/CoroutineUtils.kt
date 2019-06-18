package com.example.coreandroid.util

import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.photos.Photo
import com.flickr4java.flickr.photos.PhotoList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

private fun CoroutineScope.test() {
    launch {
        val list: PhotoList<Photo> = Flickr("", "", REST()).photosInterface.callSuspending {
            getRecent(null, 100, 1)
        }
    }
}
