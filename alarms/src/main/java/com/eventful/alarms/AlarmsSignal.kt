package com.eventful.alarms

sealed class AlarmsSignal {
    object AlarmsRemoved : AlarmsSignal()
    object AlarmAdded : AlarmsSignal()
    object AlarmUpdated : AlarmsSignal()
}
