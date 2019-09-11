package com.example.coreandroid.ticketmaster

import android.os.Parcelable
import com.example.core.model.ticketmaster.IVenue
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Venue(
    override val id: String,
    override val name: String,
    override val url: String,
    override val address: String,
    override val city: String,
    override val lat: Float,
    override val lng: Float
) : IVenue, Parcelable