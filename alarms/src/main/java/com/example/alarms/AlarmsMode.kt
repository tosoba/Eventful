package com.example.alarms

import com.example.coreandroid.model.event.Event

sealed class AlarmsMode {
    object All : AlarmsMode()
    data class SingleEvent(val event: Event) : AlarmsMode()
}