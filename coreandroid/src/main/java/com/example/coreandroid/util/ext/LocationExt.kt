package com.example.coreandroid.util.ext

import android.annotation.SuppressLint
import android.location.Address
import android.location.Location
import com.example.core.model.app.LatLng
import com.google.android.gms.location.LocationRequest
import com.patloew.rxlocation.RxLocation
import kotlinx.coroutines.rx2.await
import java.util.concurrent.TimeUnit

val Location.latLng: LatLng get() = LatLng(latitude, longitude)

suspend fun RxLocation.reverseGeocode(location: Location): Address? = geocoding()
    .fromLocation(location)
    .onErrorComplete()
    .await()

@SuppressLint("MissingPermission")
suspend fun RxLocation.currentLocation(
    timeout: Long = 10,
    unit: TimeUnit = TimeUnit.SECONDS,
    retries: Long = 1
): Location? = location()
    .updates(
        LocationRequest.create()
            .setNumUpdates(1)
            .setPriority(LocationRequest.PRIORITY_LOW_POWER)
    )
    .timeout(timeout, unit)
    .firstElement()
    .retry(retries)
    .await()
