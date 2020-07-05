package com.example.weather

import com.example.core.model.weather.Forecast

sealed class WeatherControllerData {
    object LoadingForecast : WeatherControllerData()
    data class ForecastLoaded(val forecast: Forecast) : WeatherControllerData()
    object UnknownLatLng : WeatherControllerData()
}
