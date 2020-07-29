package com.eventful.alarms

sealed class AlarmsSignal {
    object AlarmsRemoved : AlarmsSignal()
}
