package com.example.weather

import com.example.core.model.weather.Forecast
import com.example.core.util.Data
import com.example.coreandroid.controller.SnackbarState
import com.google.android.gms.maps.model.LatLng

data class WeatherState(
    val latLng: LatLng? = null,
    val forecast: Data<Forecast?> = Data(null),
    val snackbarState: SnackbarState = SnackbarState.Hidden
)
