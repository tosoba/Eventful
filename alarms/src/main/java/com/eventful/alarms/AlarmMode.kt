package com.eventful.alarms

import android.os.Parcelable
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.android.model.event.Event
import kotlinx.android.parcel.Parcelize

sealed class AlarmMode : Parcelable {
    @Parcelize
    data class Add(val event: Event) : AlarmMode()

    @Parcelize
    data class Edit(val alarm: Alarm) : AlarmMode()
}
