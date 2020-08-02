package com.eventful.core.android.model.alarm

import android.os.Parcelable
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.alarm.IAlarm
import com.eventful.core.model.event.IEvent
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Alarm(
    override val id: Long,
    override val event: Event,
    override val timestamp: Long
) : IAlarm, Parcelable {
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
