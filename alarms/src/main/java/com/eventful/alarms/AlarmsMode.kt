package com.eventful.alarms

import com.eventful.core.android.model.event.Event

sealed class AlarmsMode {
    object All : AlarmsMode()
    data class SingleEvent(val event: Event) : AlarmsMode()
}