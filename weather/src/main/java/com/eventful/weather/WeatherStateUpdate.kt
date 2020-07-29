package com.eventful.weather

import android.view.View
import com.eventful.core.model.Resource
import com.eventful.core.model.weather.Forecast
import com.eventful.core.android.controller.SnackbarAction
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.base.StateUpdate

sealed class WeatherStateUpdate : StateUpdate<WeatherState> {
    sealed class Weather : WeatherStateUpdate() {
        object Loading : Weather() {
            override fun invoke(state: WeatherState): WeatherState = state.copy(
                forecast = state.forecast.copyWithLoadingStatus,
                snackbarState = SnackbarState.Shown("Loading weather...")
            )
        }

        class Loaded(
            private val resource: Resource<Forecast>,
            private val retry: () -> Unit
        ) : Weather() {
            override fun invoke(state: WeatherState): WeatherState = when (resource) {
                is Resource.Success -> state.copy(
                    forecast = state.forecast.copyWithNewValue(resource.data),
                    snackbarState = SnackbarState.Hidden
                )
                is Resource.Error<Forecast> -> state.copy(
                    forecast = state.forecast.copyWithFailureStatus(resource.error),
                    snackbarState = SnackbarState.Shown(
                        "Unable to load weather.",
                        action = SnackbarAction("Retry", View.OnClickListener { retry() })
                    )
                )
            }
        }
    }
}
