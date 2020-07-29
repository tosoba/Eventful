package com.eventful.core.android.model.location

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationState(
    val latLng: LatLng? = null,
    val status: LocationStatus = LocationStatus.Initial
) : Parcelable
