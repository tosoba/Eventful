package com.example.coreandroid.util.ext

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat.getSystemService


val Context.isLocationAvailable: Boolean
    get() = getSystemService(this, LocationManager::class.java)?.run {
        isProviderEnabled(LocationManager.GPS_PROVIDER)
                || isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } ?: false

val Context.isConnected: Boolean
    get() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

fun Context.toDp(px: Float): Float {
    return px / resources.displayMetrics.density
}

fun Context.toPx(dp: Float): Float {
    return dp * resources.displayMetrics.density
}
