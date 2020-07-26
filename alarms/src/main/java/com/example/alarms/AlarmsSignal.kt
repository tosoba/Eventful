package com.example.alarms

sealed class AlarmsSignal {
    object AlarmsRemoved : AlarmsSignal()
}
