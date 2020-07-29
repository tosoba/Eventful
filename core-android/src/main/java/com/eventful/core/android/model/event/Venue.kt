package com.eventful.core.android.model.event

import android.os.Parcelable
import com.eventful.core.model.event.IVenue
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Venue(
    override val id: String,
    override val name: String?,
    override val url: String?,
    override val address: String?,
    override val city: String?,
    override val lat: Double?,
    override val lng: Double?
) : IVenue, Parcelable {
    constructor(other: IVenue) : this(
        other.id,
        other.name,
        other.url,
        other.address,
        other.city,
        other.lat,
        other.lng
    )

    val latLng: LatLng? get() = if (lat != null && lng != null) LatLng(lat, lng) else null
}
