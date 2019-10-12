package com.example.coreandroid.util.ext

import android.content.Context
import android.location.Address
import android.location.Location
import android.os.Build
import android.provider.Settings
import com.example.core.model.app.LatLng
import com.google.android.gms.location.LocationRequest
import com.patloew.rxlocation.RxLocation
import kotlinx.coroutines.rx2.await
import java.util.concurrent.TimeUnit

val Location.latLng: LatLng
    get() = LatLng(latitude, longitude)

suspend fun RxLocation.reverseGeocode(location: Location): Address? = geocoding()
    .fromLocation(location)
    .onErrorComplete()
    .await()

//TODO: timeout from settings
suspend fun RxLocation.currentLocation(
    timeout: Long = 15,
    unit: TimeUnit = TimeUnit.SECONDS
): Location? = location()
    .updates(
        LocationRequest.create()
            .setNumUpdates(1)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    )
    .timeout(timeout, unit)
    .firstElement()
    .onErrorResumeNext(location().lastLocation())
    .switchIfEmpty(location().lastLocation())
    .await()

val Context.locationEnabled: Boolean
    get() = if (Build.VERSION.SDK_INT >= 19) try {
        Settings.Secure.getInt(contentResolver, "location_mode")
    } catch (var3: Settings.SettingNotFoundException) {
        0
    } != 0
    else Settings.Secure.getString(contentResolver, "location_providers_allowed").isNotEmpty()
