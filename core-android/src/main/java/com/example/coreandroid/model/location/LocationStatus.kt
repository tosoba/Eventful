package com.example.coreandroid.model.location

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class LocationStatus : Parcelable {
    @Parcelize
    object Initial : LocationStatus()

    @Parcelize
    object PermissionDenied : LocationStatus()

    @Parcelize
    object Disabled : LocationStatus()

    @Parcelize
    object Loading : LocationStatus()

    @Parcelize
    class Error(val throwable: Throwable) : LocationStatus()

    @Parcelize
    object Found : LocationStatus()
}
