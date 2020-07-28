package com.example.alarms

import com.example.core.util.DataList
import com.example.coreandroid.base.SelectableItemsSnackbarState
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.model.alarm.Alarm
import com.example.core.model.Selectable

data class AlarmsState(
    val mode: AlarmsMode,
    override val items: DataList<Selectable<Alarm>> = DataList(),
    val snackbarState: SnackbarState = SnackbarState.Hidden
) : SelectableItemsSnackbarState<AlarmsState, Alarm> {

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
}
