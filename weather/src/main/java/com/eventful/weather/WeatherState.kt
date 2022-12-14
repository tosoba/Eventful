package com.eventful.weather

import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.weather.Forecast
import com.eventful.core.util.Data

data class WeatherState(
    val event: Event,
    val tab: WeatherTab = WeatherTab.NOW,
    val forecastNow: Data<Forecast?> = Data(null),
    val forecastEventTime: Data<Forecast?> = Data(null),
    val snackbarState: SnackbarState = SnackbarState.Hidden
)
