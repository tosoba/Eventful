package com.example.coreandroid.model.alarm

import com.example.core.model.alarm.IAlarm
import com.example.core.model.event.IEvent
import com.example.coreandroid.model.event.Event
import java.text.SimpleDateFormat
import java.util.*

data class Alarm(
    override val id: Long,
    override val event: Event,
    override val timestamp: Long
) : IAlarm {
    constructor(other: IAlarm) : this(other.id, Event(other.event), other.timestamp)
    constructor(id: Long, event: IEvent, timestamp: Long) : this(id, Event(event), timestamp)

    companion object {
        fun from(other: IAlarm): Alarm = if (other is Alarm) other else Alarm(other)
    }

    val formattedTime: String
        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

    val formattedDate: String
        get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
}
