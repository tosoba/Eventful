package com.eventful.alarms.dialog

import android.os.Parcelable
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.android.model.event.Event
import kotlinx.android.parcel.Parcelize
import java.util.*

sealed class AddEditAlarmDialogMode : Parcelable {
    @Parcelize
    data class Add(val event: Event) : AddEditAlarmDialogMode()

    @Parcelize
    data class Edit(val alarm: Alarm) : AddEditAlarmDialogMode()

    val hour: Int
        get() = when (this) {
            is Add -> {
                val startTime = requireNotNull(event.startTime)
                val splitTime = startTime.split(':')
                splitTime.first().toInt()
            }
            is Edit -> {
                val calendar = GregorianCalendar.getInstance()
                calendar.time = Date(alarm.timestamp)
                calendar[Calendar.HOUR_OF_DAY]
            }
        }

    val minute: Int
        get() = when (this) {
            is Add -> {
                val startTime = requireNotNull(event.startTime)
                val splitTime = startTime.split(':')
                splitTime.last().toInt()
            }
            is Edit -> {
                val calendar = GregorianCalendar.getInstance()
                calendar.time = Date(alarm.timestamp)
                calendar[Calendar.MINUTE]
            }
        }

    val startDateCalendar: Calendar
        get() = GregorianCalendar.getInstance().apply {
            when (this@AddEditAlarmDialogMode) {
                is Add -> {
                    time = requireNotNull(event.startDate)
                    add(Calendar.DATE, -1)
                }
                is Edit -> {
                    time = Date(alarm.timestamp)
                }
            }
        }
}
