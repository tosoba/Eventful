package com.eventful.alarms

import com.eventful.alarms.dialog.AddEditAlarmDialogMode
import com.eventful.alarms.dialog.AddEditAlarmDialogState
import com.eventful.alarms.dialog.AddEditAlarmDialogStatus
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.model.Selectable
import com.eventful.core.util.HoldsList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed class AlarmsViewUpdate {
    data class Events(val alarms: HoldsList<Selectable<Alarm>>) : AlarmsViewUpdate()
    data class Snackbar(val state: SnackbarState) : AlarmsViewUpdate()
    data class UpdateActionMode(val numberOfSelectedAlarms: Int) : AlarmsViewUpdate()
    object FinishActionMode : AlarmsViewUpdate()
    data class ShowDialog(
        val mode: AddEditAlarmDialogMode,
        val previousState: AddEditAlarmDialogState?
    ) : AlarmsViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
val AlarmsViewModel.resumedOnlyViewUpdates: Flow<AlarmsViewUpdate>
    get() = merge(
        states.map { it.snackbarState }
            .distinctUntilChanged()
            .map { AlarmsViewUpdate.Snackbar(it) },
        states.map { state -> state.items.data.count { it.selected } }
            .distinctUntilChanged()
            .map { AlarmsViewUpdate.UpdateActionMode(it) },
        signals.filterIsInstance<AlarmsSignal.AlarmsRemoved>()
            .map { AlarmsViewUpdate.FinishActionMode }
    )

@ExperimentalCoroutinesApi
@FlowPreview
val AlarmsViewModel.viewUpdates: Flow<AlarmsViewUpdate>
    get() = merge(
        states.map { it.items }
            .distinctUntilChanged()
            .map { AlarmsViewUpdate.Events(it) },
        states.map { it.dialogStatus }
            .filterIsInstance<AddEditAlarmDialogStatus.WithMode>()
            .map {
                AlarmsViewUpdate.ShowDialog(
                    mode = it.mode,
                    previousState = when (it) {
                        is AddEditAlarmDialogStatus.WithMode.Shown -> null
                        is AddEditAlarmDialogStatus.WithMode.ShownWithState -> it.state
                    }
                )
            }
    )
