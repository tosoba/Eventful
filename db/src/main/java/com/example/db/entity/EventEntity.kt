package com.example.db.entity

import androidx.room.Entity
import com.example.db.Tables
import java.util.*

@Entity(tableName = Tables.EVENT, primaryKeys = ["id"])
data class EventEntity(
    val id: String,
    val url: String,
    val imageUrl: String,
    val distance: Float?,
    val salesStartDate: Date?,
    val salesEndDate: Date?,
    val startDate: Date?,
    val startTime: String?,
    val kinds: List<String>,
    val priceRanges: List<PriceRangeEntity>?
)