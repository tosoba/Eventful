package com.example.db.entity

import androidx.room.Entity
import com.example.db.Tables

@Entity(tableName = Tables.VENUE, primaryKeys = ["id"])
data class VenueEntity(
    val id: String,
    val name: String,
    val url: String?,
    val address: String?,
    val city: String,
    val lat: Float,
    val lng: Float
)