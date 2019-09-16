package com.example.coreandroid.util.ext

import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService


val Context.isLocationAvailable: Boolean
    get() = getSystemService(this, LocationManager::class.java)?.let {
        return@let it.isProviderEnabled(LocationManager.GPS_PROVIDER) || it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } ?: false
