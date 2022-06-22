package com.eventful

import com.eventful.core.android.base.StateUpdate
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.model.location.LocationStatus
import com.eventful.core.model.alarm.IAlarm
import com.eventful.core.model.event.IEvent
import com.eventful.core.model.location.LocationResult
import com.google.android.gms.maps.model.LatLng

sealed class MainStateUpdate : StateUpdate<MainState> {
    class Connection(private val connected: Boolean) : MainStateUpdate() {
        override fun invoke(state: MainState): MainState = state.copy(connected = connected)
    }

    sealed class Location : MainStateUpdate() {
        object PermissionDenied : MainStateUpdate() {
            override fun invoke(state: MainState): MainState =
                state.copy(
                    locationState =
                        state.locationState.copy(status = LocationStatus.PermissionDenied))
        }

        object Reset : MainStateUpdate() {
            override fun invoke(state: MainState): MainState =
                state.copy(
                    locationState = state.locationState.copy(status = LocationStatus.Initial))
        }

        class Result(private val result: LocationResult) : Location() {
            override fun invoke(state: MainState): MainState =
                state.copy(
                    locationState =
                        if (result is LocationResult.Found)
                            state.locationState.copy(
                                latLng = LatLng(result.latitude, result.longitude),
                                status = LocationStatus.Found)
                        else
                            state.locationState.copy(
                                status =
                                    when (result) {
                                        is LocationResult.Loading -> LocationStatus.Loading
                                        is LocationResult.Disabled -> LocationStatus.Disabled
                                        is LocationResult.Error ->
                                            LocationStatus.Error(result.throwable)
                                        else -> throw IllegalArgumentException()
                                    }))
        }
    }

    data class UpcomingEvents(private val events: List<IEvent>) : MainStateUpdate() {
        override fun invoke(state: MainState): MainState =
            state.copy(upcomingEvents = events.map { Event(it) })
    }

    data class UpcomingAlarms(private val alarms: List<IAlarm>) : MainStateUpdate() {
        override fun invoke(state: MainState): MainState =
            state.copy(upcomingAlarms = alarms.map(Alarm.Companion::from))
    }
}
