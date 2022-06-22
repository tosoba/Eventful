package com.eventful.event

import com.eventful.event.alarms.AlarmsEventValidator
import com.eventful.weather.WeatherEventValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

sealed class EventViewUpdate {
    data class NewViewPager(val includeWeather: Boolean, val includeAlarms: Boolean) :
        EventViewUpdate()

    object Pop : EventViewUpdate()
}

@FlowPreview
@ExperimentalCoroutinesApi
val EventViewModel.viewUpdates: Flow<EventViewUpdate>
    get() =
        merge(
            states.filter { it.events.isEmpty() }.map { EventViewUpdate.Pop },
            states
                .filter { it.events.isNotEmpty() }
                .map { (events) ->
                    val currentEvent = events.last()
                    EventViewUpdate.NewViewPager(
                        includeWeather = WeatherEventValidator.isValid(currentEvent),
                        includeAlarms = AlarmsEventValidator.isValid(currentEvent))
                })
