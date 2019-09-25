package com.example.coreandroid.util.ext

import android.location.Address
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.patloew.rxlocation.RxLocation
import io.nlopez.smartlocation.SmartLocation
import kotlinx.coroutines.rx2.await
import java.util.concurrent.TimeUnit
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

suspend fun RxLocation.currentLocation(): Location? = location()
    .lastLocation()
    .switchIfEmpty(
        location().updates(
            LocationRequest.create().setNumUpdates(1)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        ).timeout(15, TimeUnit.SECONDS).firstElement().onErrorComplete()
    ).await()
