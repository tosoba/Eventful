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

    data class ForecastLoaded(val forecast: Forecast, val city: String, val tab: WeatherTab) :
        WeatherViewUpdate()

    data class Snackbar(val state: SnackbarState) : WeatherViewUpdate()
}

@ExperimentalCoroutinesApi
@FlowPreview
val WeatherViewModel.viewUpdates: Flow<WeatherViewUpdate>
    get() =
        merge(
            states
                .map { it.snackbarState }
                .distinctUntilChanged()
                .map { WeatherViewUpdate.Snackbar(it) },
            states
                .filter { (_, tab, forecastNow, forecastEventTime) ->
                    (forecastNow.status is Loading && tab == WeatherTab.NOW) ||
                        (forecastEventTime.status is Loading && tab == WeatherTab.EVENT_TIME)
                }
                .map { WeatherViewUpdate.LoadingForecast },
            states
                .filter { (_, tab, forecastNow, forecastEventTime) ->
                    (forecastNow.status is LoadedSuccessfully && tab == WeatherTab.NOW) ||
                        (forecastEventTime.status is LoadedSuccessfully &&
                            tab == WeatherTab.EVENT_TIME)
                }
                .map { (event, tab, forecastNow, forecastEventTime) ->
                    WeatherViewUpdate.ForecastLoaded(
                        when (tab) {
                            WeatherTab.NOW -> requireNotNull(forecastNow.data)
                            WeatherTab.EVENT_TIME -> requireNotNull(forecastEventTime.data)
                        },
                        requireNotNull(event.venues?.firstOrNull()?.city),
                        tab)
                })
