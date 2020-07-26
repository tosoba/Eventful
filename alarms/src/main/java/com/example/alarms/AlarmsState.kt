package com.example.alarms

import com.example.core.util.DataList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.alarm.Alarm
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable

data class AlarmsState(
    val event: Event?,
    val items: DataList<Selectable<Alarm>> = DataList(),
    val limit: Int = 0,
    val snackbarState: SnackbarState = SnackbarState.Hidden
)
