package com.eventful.weather

import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.model.weather.Forecast
import com.eventful.core.util.LoadedSuccessfully
import com.eventful.core.util.Loading
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

sealed class WeatherViewUpdate {
    object LoadingForecast : WeatherViewUpdate()
    data class ForecastLoaded(val forecast: Forecast, val city: String) : WeatherViewUpdate()
    data class Snackbar(val state: SnackbarState) : WeatherViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
val WeatherViewModel.viewUpdates: Flow<WeatherViewUpdate>
    get() = merge(
        states.map { it.snackbarState }
            .distinctUntilChanged()
            .map { WeatherViewUpdate.Snackbar(it) },
        states.filter {
            (it.forecastNow.status is Loading && it.tab == WeatherTab.NOW)
                    || (it.forecastEventTime.status is Loading && it.tab == WeatherTab.EVENT_TIME)
        }.map { WeatherViewUpdate.LoadingForecast },
        states.filter {
            (it.forecastNow.status is LoadedSuccessfully && it.tab == WeatherTab.NOW)
                    || (it.forecastEventTime.status is LoadedSuccessfully && it.tab == WeatherTab.EVENT_TIME)
        }.map { (event, _, forecast) ->
            WeatherViewUpdate.ForecastLoaded(
                requireNotNull(forecast.data),
                requireNotNull(event.venues?.firstOrNull()?.city)
            )
        }
    )
