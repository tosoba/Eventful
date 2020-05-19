package com.example.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.example.core.model.ticketmaster.IEvent
import com.example.db.Tables
import java.util.*

@Entity(tableName = Tables.EVENT, primaryKeys = ["id"])
data class EventEntity(
    val id: String,
    val name: String,
    val url: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    val distance: Float?,
    val info: String?,
    @ColumnInfo(name = "sales_start_date") val salesStartDate: Date?,
    @ColumnInfo(name = "sales_end_date") val salesEndDate: Date?,
    @ColumnInfo(name = "start_date") val startDate: Date?,
    @ColumnInfo(name = "start_time") val startTime: String?,
    val kinds: List<String>?,
    @ColumnInfo(name = "price_ranges") val priceRanges: List<PriceRangeEntity>?,
    @ColumnInfo(name = "date_saved") val dateSaved: Date
) {
    constructor(other: IEvent) : this(
        other.id,
        other.name,
        other.url,
        other.imageUrl,
        other.distance,
        other.info,
        other.salesStartDate,
        other.salesEndDate,
        other.startDate,
        other.startTime,
        other.kinds,
        other.priceRanges?.map { PriceRangeEntity(it) },
        Date()
    )
}