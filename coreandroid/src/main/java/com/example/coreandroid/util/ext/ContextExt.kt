package com.example.coreandroid.util.ext

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService


val Context.isLocationAvailable: Boolean
    get() = getSystemService(this, LocationManager::class.java)?.let {
        return@let it.isProviderEnabled(LocationManager.GPS_PROVIDER) || it.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    } ?: false

val Context.isConnected: Boolean
    get() {
        val connectivityManager: ConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

