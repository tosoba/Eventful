package com.eventful.weather

import android.view.View
import com.eventful.core.android.base.StateUpdate
import com.eventful.core.android.controller.SnackbarAction
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Resource
import com.eventful.core.model.weather.Forecast
import com.eventful.core.util.Data

sealed class WeatherStateUpdate : StateUpdate<WeatherState> {
    data class NewEvent(val event: Event) : WeatherStateUpdate() {
        override fun invoke(state: WeatherState): WeatherState = state.copy(event = event)
    }

    data class TabSelected(val tab: WeatherTab) : WeatherStateUpdate() {
        override fun invoke(state: WeatherState): WeatherState = state.copy(tab = tab)
    }

    sealed class Weather : WeatherStateUpdate() {
        data class Loading(val tab: WeatherTab) : Weather() {
            override fun invoke(state: WeatherState): WeatherState = state.copy(
                forecastNow = when (tab) {
                    WeatherTab.NOW -> state.forecastNow.copyWithLoadingStatus
                    WeatherTab.EVENT_TIME -> state.forecastNow
                },
                forecastEventTime = when (tab) {
                    WeatherTab.NOW -> state.forecastEventTime
                    WeatherTab.EVENT_TIME -> state.forecastEventTime.copyWithLoadingStatus
                },
                snackbarState = SnackbarState.Shown("Loading weather...")
            )
        }

        data class Loaded(
            val resource: Resource<Forecast>,
            val tab: WeatherTab,
            val newEvent: Boolean,
            val retry: () -> Unit
        ) : Weather() {
            override fun invoke(state: WeatherState): WeatherState = when (resource) {
                is Resource.Success -> when (tab) {
                    WeatherTab.NOW -> state.copy(
                        forecastNow = state.forecastNow.copyWithNewValue(resource.data),
                        forecastEventTime = if (newEvent) Data<Forecast?>(null) else state.forecastEventTime,
                        snackbarState = SnackbarState.Hidden
                    )
                    WeatherTab.EVENT_TIME -> state.copy(
                        forecastNow = if (newEvent) Data<Forecast?>(null) else state.forecastNow,
                        forecastEventTime = state.forecastEventTime.copyWithNewValue(resource.data),
                        snackbarState = SnackbarState.Hidden
                    )
                }
                is Resource.Error<Forecast> -> when (tab) {
                    WeatherTab.NOW -> state.copy(
                        forecastNow = state.forecastNow.copyWithFailureStatus(resource.error),
                        forecastEventTime = if (newEvent) Data<Forecast?>(null) else state.forecastEventTime,
                        snackbarState = SnackbarState.Shown(
                            "Unable to load weather.",
                            action = SnackbarAction("Retry", View.OnClickListener { retry() })
                        )
                    )
                    WeatherTab.EVENT_TIME -> state.copy(
                        forecastNow = if (newEvent) Data<Forecast?>(null) else state.forecastNow,
                        forecastEventTime = state.forecastEventTime.copyWithFailureStatus(resource.error),
                        snackbarState = SnackbarState.Shown(
                            "Unable to load weather.",
                            action = SnackbarAction("Retry", View.OnClickListener { retry() })
                        )
                    )
                }
            }
        }
    }
}
