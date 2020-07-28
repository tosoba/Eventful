package com.example.alarms

import com.example.core.model.Selectable
import com.example.core.util.HoldsList
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.alarm.Alarm
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed class AlarmsViewUpdate {
    data class Events(val alarms: HoldsList<Selectable<Alarm>>) : AlarmsViewUpdate()
    data class Snackbar(val state: SnackbarState) : AlarmsViewUpdate()
    data class UpdateActionMode(val numberOfSelectedAlarms: Int) : AlarmsViewUpdate()
    object FinishActionMode : AlarmsViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
val AlarmsViewModel.viewUpdates: Flow<AlarmsViewUpdate>
    get() = merge(
        states.map { it.items }
            .distinctUntilChanged()
            .map { AlarmsViewUpdate.Events(it) },
        states.map { it.snackbarState }
            .distinctUntilChanged()
            .map { AlarmsViewUpdate.Snackbar(it) },
        states.map { state -> state.items.data.count { it.selected } }
            .distinctUntilChanged()
            .map { AlarmsViewUpdate.UpdateActionMode(it) },
        signals.filterIsInstance<AlarmsSignal.AlarmsRemoved>()
            .map { AlarmsViewUpdate.FinishActionMode }
    )
