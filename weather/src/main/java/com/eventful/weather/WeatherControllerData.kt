package com.eventful.weather

import com.eventful.core.model.weather.Forecast

sealed class WeatherControllerData {
    object LoadingForecast : WeatherControllerData()
    data class ForecastLoaded(val forecast: Forecast) : WeatherControllerData()
    object UnknownLatLng : WeatherControllerData()
}
