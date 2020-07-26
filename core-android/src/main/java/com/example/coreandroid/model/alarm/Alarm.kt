package com.example.coreandroid.model.alarm

import com.example.core.model.alarm.IAlarm

data class Alarm(
    override val id: Long,
    override val eventId: String,
    override val timestamp: Long
) : IAlarm {
    constructor(other: IAlarm) : this(other.id, other.eventId, other.timestamp)
}
