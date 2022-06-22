package com.eventful.alarms

import com.eventful.alarms.dialog.AddEditAlarmDialogStatus
import com.eventful.core.android.base.ClearSelectionUpdate
import com.eventful.core.android.base.ItemSelectionConfirmedUpdate
import com.eventful.core.android.base.StateUpdate
import com.eventful.core.android.base.ToggleItemSelectionUpdate
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable
import com.eventful.core.model.alarm.IAlarm
import com.eventful.core.util.DataList
import com.eventful.core.util.LoadedSuccessfully
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
sealed class AlarmsStateUpdate : StateUpdate<AlarmsState> {
    data class NewEvent(val event: Event) : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState =
            if (state.mode !is AlarmsMode.SingleEvent) {
                throw IllegalStateException()
            } else {
                state.copy(mode = state.mode.copy(event))
            }
    }

    data class Alarms(val alarms: List<IAlarm>) : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState =
            state.copy(
                items =
                    DataList(
                        data = alarms.map { Selectable(Alarm.from(it)) },
                        status = LoadedSuccessfully,
                        limitHit = state.items.data.size == alarms.size))
    }

    data class ToggleAlarmSelection(override val item: Alarm) :
        AlarmsStateUpdate(), ToggleItemSelectionUpdate<AlarmsState, Alarm, Int> {
        override fun Alarm.id(): Int = id
    }

    object ClearSelection : AlarmsStateUpdate(), ClearSelectionUpdate<AlarmsState, Alarm>

    object HideSnackbar : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState =
            state.copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
    }

    data class AlarmAdded(val onSnackbarDismissed: () -> Unit) : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState =
            state.copy(
                snackbarState =
                    SnackbarState.Shown(
                        R.string.alarm_added,
                        Snackbar.LENGTH_SHORT,
                        onDismissed = onSnackbarDismissed))
    }

    data class AlarmUpdated(val onSnackbarDismissed: () -> Unit) : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState =
            state.copy(
                snackbarState =
                    SnackbarState.Shown(
                        R.string.alarm_updated,
                        Snackbar.LENGTH_SHORT,
                        onDismissed = onSnackbarDismissed))
    }

    data class RemovedAlarms(
        override val msgRes: SnackbarState.Shown.MsgRes,
        override val onSnackbarDismissed: () -> Unit
    ) : AlarmsStateUpdate(), ItemSelectionConfirmedUpdate<AlarmsState, Alarm>

    data class DialogStatus(val status: AddEditAlarmDialogStatus) : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState = state.copy(dialogStatus = status)
    }
}
