package com.eventful.alarms

import com.eventful.alarms.dialog.AddEditAlarmDialogStatus
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.android.model.event.Event

sealed class AlarmsIntent {
    object LoadMoreAlarms : AlarmsIntent()
    data class AddAlarm(val event: Event, val timestamp: Long) : AlarmsIntent()
    data class UpdateAlarm(val id: Int, val timestamp: Long) : AlarmsIntent()
    object RemoveAlarmsClicked : AlarmsIntent()
    data class AlarmLongClicked(val alarm: Alarm) : AlarmsIntent()
    object ClearSelectionClicked : AlarmsIntent()
    object HideSnackbar : AlarmsIntent()
    data class UpdateDialogStatus(val status: AddEditAlarmDialogStatus) : AlarmsIntent()
}
