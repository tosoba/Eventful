package com.example.coreandroid.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import io.nlopez.smartlocation.SmartLocation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun SmartLocation.LocationControl.awaitOne(): Location = suspendCoroutine { continuation ->
    oneFix().start { continuation.resume(it) }
}

val Location.latLng: LatLng
    get() = LatLng(latitude, longitude)