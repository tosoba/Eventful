package com.eventful.core.android.util.ext

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat.getSystemService

val Context.isLocationAvailable: Boolean
    get() = getSystemService(this, LocationManager::class.java)?.run {
        isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    } ?: false

val Context.isConnected: Boolean
    get() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

fun Context.toDp(px: Float): Float = px / resources.displayMetrics.density

fun Context.toPx(dp: Float): Float = dp * resources.displayMetrics.density

fun Context.themeColor(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}