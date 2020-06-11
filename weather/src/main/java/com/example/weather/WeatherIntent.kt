package com.example.weather

import com.google.android.gms.maps.model.LatLng

sealed class WeatherIntent {
    data class LoadWeather(val latLng: LatLng) : WeatherIntent()
}