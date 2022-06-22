package com.eventful.core.android.util.ext

import android.content.Context
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.ConnectivityManager
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

val Context.isLocationAvailable: Boolean
    get() =
        getSystemService(this, LocationManager::class.java)?.run {
            isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
            ?: false

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

suspend fun Context.loadBitmap(url: String): Bitmap? = suspendCoroutine {
    Glide.with(this)
        .asBitmap()
        .load(url)
        .listener(
            object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean = it.resume(null).let { false }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean = it.resume(resource).let { false }
            })
        .submit()
}
