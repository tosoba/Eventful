package com.example.weather

import android.view.View
import com.example.core.model.Resource
import com.example.core.model.weather.Forecast
import com.example.coreandroid.controller.SnackbarAction
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.util.StateUpdate

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
