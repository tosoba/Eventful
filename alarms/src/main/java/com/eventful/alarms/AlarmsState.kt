package com.eventful.alarms

import com.eventful.core.util.DataList
import com.eventful.core.android.base.SelectableItemsSnackbarState
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.model.Selectable

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
