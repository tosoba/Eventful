package com.eventful.alarms

import com.eventful.core.android.model.event.Event

object AlarmsEventValidator {
    fun isValid(event: Event): Boolean = event.startDateTimeSetInFuture
}
