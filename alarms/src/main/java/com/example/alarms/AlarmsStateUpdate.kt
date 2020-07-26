package com.example.alarms

import com.example.core.model.alarm.IAlarm
import com.example.core.util.DataList
import com.example.core.util.LoadedSuccessfully
import com.example.coreandroid.base.StateUpdate
import com.example.coreandroid.model.alarm.Alarm
import com.example.coreandroid.model.event.Selectable

sealed class AlarmsStateUpdate : StateUpdate<AlarmsState> {
    data class Alarms(val alarms: List<IAlarm>) : AlarmsStateUpdate() {
        override fun invoke(state: AlarmsState): AlarmsState = state.copy(
            items = DataList(
                data = alarms.map { Selectable(Alarm(it)) },
                status = LoadedSuccessfully,
                limitHit = state.items.data.size == alarms.size
            ),
            limit = alarms.size
        )
    }
}
