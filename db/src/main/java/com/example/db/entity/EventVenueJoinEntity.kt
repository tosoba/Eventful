package com.example.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.db.Tables

@Entity(
    tableName = Tables.EVENT_ATTRACTION_JOIN,
    primaryKeys = ["event_id", "venue_id"],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            childColumns = ["event_id"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VenueEntity::class,
            childColumns = ["venue_id"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EventVenueJoinEntity(
    @ColumnInfo(name = "event_id") val eventId: String,
    @ColumnInfo(name = "venue_id") val venueId: String
)