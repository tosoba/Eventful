package com.example.alarms

import com.example.coreandroid.model.alarm.Alarm

sealed class AlarmsIntent {
    data class AddAlarm(val alarm: Alarm) : AlarmsIntent()
    data class DeleteAlarms(val ids: List<Long>) : AlarmsIntent()
}
