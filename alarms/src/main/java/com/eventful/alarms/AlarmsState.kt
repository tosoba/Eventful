package com.eventful.alarms

import androidx.lifecycle.SavedStateHandle
import com.eventful.alarms.dialog.AddEditAlarmDialogStatus
import com.eventful.core.android.base.SelectableItemsSnackbarState
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.model.Selectable
import com.eventful.core.util.DataList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
data class AlarmsState(
    val mode: AlarmsMode,
    val dialogStatus: AddEditAlarmDialogStatus = AddEditAlarmDialogStatus.Hidden,
    override val items: DataList<Selectable<Alarm>> = DataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableItemsSnackbarState<AlarmsState, Alarm> {

    constructor(savedStateHandle: SavedStateHandle) : this(
        mode = savedStateHandle[AlarmsFragment.MODE_ARG_KEY]!!,
        dialogStatus = savedStateHandle[KEY_DIALOG_STATUS] ?: AddEditAlarmDialogStatus.Hidden
    )

    override fun copyWithTransformedItems(
        transform: (Selectable<Alarm>) -> Selectable<Alarm>
    ): AlarmsState = copy(items = items.transformItems(transform))

    override fun copyWithSnackbarStateAndTransformedItems(
        snackbarState: SnackbarState,
        transform: (Selectable<Alarm>) -> Selectable<Alarm>
    ): AlarmsState = copy(
        items = items.transformItems(transform),
        snackbarState = snackbarState
    )

    override fun copyWithSnackbarState(snackbarState: SnackbarState): AlarmsState = copy(
        snackbarState = snackbarState
    )

    companion object {
        const val KEY_DIALOG_STATUS = "key_dialog_status"
    }
}
