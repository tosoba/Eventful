package com.eventful.db.entity

import androidx.room.*
import com.eventful.db.Tables

@Entity(
    tableName = Tables.ALARM,
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            childColumns = ["event_id"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["event_id"], name = "alarm_event_id_idx")
    ]
)
data class AlarmEntity(
    @ColumnInfo(name = "event_id") val eventId: String,
    val timestamp: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
