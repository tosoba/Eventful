package com.eventful.weather

import com.eventful.core.model.weather.Forecast
import com.eventful.core.util.Data
import com.eventful.core.android.controller.SnackbarState
import com.google.android.gms.maps.model.LatLng

data class WeatherState(
    val latLng: LatLng? = null,
    val forecast: Data<Forecast?> = Data(null),
    val snackbarState: SnackbarState = SnackbarState.Hidden
)
