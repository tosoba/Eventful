package com.eventful.alarms

import com.eventful.alarms.dialog.AddEditAlarmDialogStatus
import com.eventful.core.android.model.alarm.Alarm

sealed class AlarmsIntent {
    object LoadAlarms : AlarmsIntent()
    data class AddAlarm(val alarm: Alarm) : AlarmsIntent()
    object RemoveAlarmsClicked : AlarmsIntent()
    data class AlarmLongClicked(val alarm: Alarm) : AlarmsIntent()
    object ClearSelectionClicked : AlarmsIntent()
    object HideSnackbar : AlarmsIntent()
    data class UpdateDialogStatus(val status: AddEditAlarmDialogStatus) : AlarmsIntent()
}
