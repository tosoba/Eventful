package com.eventful

import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.android.model.event.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

sealed class MainViewUpdate {
    data class DrawerMenu(
        val upcomingAlarms: List<Alarm>,
        val upcomingEvents: List<Event>
    ) : MainViewUpdate()
}

@FlowPreview
@ExperimentalCoroutinesApi
val MainViewModel.viewUpdates: Flow<MainViewUpdate>
    get() = states.map { MainViewUpdate.DrawerMenu(it.upcomingAlarms, it.upcomingEvents) }
        .distinctUntilChanged()
