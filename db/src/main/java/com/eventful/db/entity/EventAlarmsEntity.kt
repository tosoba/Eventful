package com.eventful.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class EventAlarmsEntity(
    @Embedded val event: FullEventEntity,
    @Relation(parentColumn = "id", entityColumn = "event_id") val alarms: List<AlarmEntity>
)
