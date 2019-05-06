package com.example.coreandroid.util

import android.location.Address
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.patloew.rxlocation.RxLocation
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.rx2.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun SmartLocation.LocationControl.awaitOne(): Location = suspendCoroutine { continuation ->
    oneFix().start { continuation.resume(it) }
}

val Location.latLng: LatLng
    get() = LatLng(latitude, longitude)

suspend fun RxLocation.reverseGeocode(location: Location): Address? = geocoding()
    .fromLocation(location)
    .onErrorComplete()
    .await()
