package com.example.db.entity

import androidx.room.Entity
import com.example.core.model.ticketmaster.IVenue
import com.example.db.Tables

@Entity(tableName = Tables.VENUE, primaryKeys = ["id"])
data class VenueEntity(
    override val id: String,
    override val name: String?,
    override val url: String?,
    override val address: String?,
    override val city: String?,
    override val lat: Double?,
    override val lng: Double?
) : IVenue {
    constructor(other: IVenue) : this(
        other.id, other.name, other.url, other.address, other.city, other.lat, other.lng
    )
}