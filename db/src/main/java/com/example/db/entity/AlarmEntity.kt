package com.example.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.core.model.alarm.IAlarm
import com.example.db.Tables

@Entity(
    tableName = Tables.ALARM,
    primaryKeys = ["id"],
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
    override val id: Long,
    @ColumnInfo(name = "event_id") override val eventId: String,
    override val timestamp: Long
) : IAlarm {
    constructor(other: IAlarm) : this(other.id, other.eventId, other.timestamp)
}
