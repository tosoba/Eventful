package com.example.weather

import com.example.core.model.weather.Forecast
import com.example.core.util.LoadedSuccessfully
import com.example.core.util.Loading
import com.example.coreandroid.controller.SnackbarState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed class WeatherViewUpdate {
    object UnknownLatLng : WeatherViewUpdate()
    object LoadingForecast : WeatherViewUpdate()
    data class ForecastLoaded(val forecast: Forecast) : WeatherViewUpdate()
    data class Snackbar(val state: SnackbarState) : WeatherViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
val WeatherViewModel.viewUpdates: Flow<WeatherViewUpdate>
    get() = merge(
        states.map { it.snackbarState }
            .distinctUntilChanged()
            .map { WeatherViewUpdate.Snackbar(it) },
        states.map { it.forecast }
            .filter { (_, status) -> status is Loading }
            .map { WeatherViewUpdate.LoadingForecast },
        states.map { it.forecast }
            .filter { (_, status) -> status is LoadedSuccessfully }
            .map { it.data }
            .filterNotNull()
            .map { WeatherViewUpdate.ForecastLoaded(it) },
        states.map { it.latLng }
            .filter { it == null }
            .map { WeatherViewUpdate.UnknownLatLng }
    )
