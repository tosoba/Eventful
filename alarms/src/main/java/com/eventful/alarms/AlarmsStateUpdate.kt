package com.eventful.alarms

import com.eventful.alarms.dialog.AddEditAlarmDialogStatus
import com.eventful.core.android.base.ClearSelectionUpdate
import com.eventful.core.android.base.ItemSelectionConfirmedUpdate
import com.eventful.core.android.base.StateUpdate
import com.eventful.core.android.base.ToggleItemSelectionUpdate
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.model.Selectable
import com.eventful.core.model.alarm.IAlarm
import com.eventful.core.util.DataList
import com.eventful.core.util.LoadedSuccessfully
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
sealed class AlarmsStateUpdate : StateUpdate<AlarmsState> {
    data class Alarms(val alarms: List<IAlarm>) : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState = state.copy(
            items = DataList(
                data = alarms.map { Selectable(Alarm(it)) },
                status = LoadedSuccessfully,
                limitHit = state.items.data.size == alarms.size
            )
        )
    }

    data class ToggleAlarmSelection(
        override val item: Alarm
    ) : AlarmsStateUpdate(),
        ToggleItemSelectionUpdate<AlarmsState, Alarm, Int> {
        override fun Alarm.id(): Int = id
    }

    object ClearSelection : AlarmsStateUpdate(), ClearSelectionUpdate<AlarmsState, Alarm>

    object HideSnackbar : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState = state
            .copyWithSnackbarState(snackbarState = SnackbarState.Hidden)
    }

    data class RemovedAlarms(
        override val snackbarText: String,
        override val onSnackbarDismissed: () -> Unit
    ) : AlarmsStateUpdate(),
        ItemSelectionConfirmedUpdate<AlarmsState, Alarm>

    data class DialogStatus(val status: AddEditAlarmDialogStatus) : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState = state.copy(
            dialogStatus = status
        )
    }
}
