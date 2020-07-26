package com.example.alarms

import com.example.coreandroid.model.alarm.Alarm

sealed class AlarmsIntent {
    object LoadAlarms : AlarmsIntent()
    data class AddAlarm(val alarm: Alarm) : AlarmsIntent()
    object RemoveAlarmsClicked : AlarmsIntent()
    data class AlarmLongClicked(val alarm: Alarm) : AlarmsIntent()
    object ClearSelectionClicked : AlarmsIntent()
    object HideSnackbar : AlarmsIntent()
}
