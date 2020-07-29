package com.eventful.weather

sealed class WeatherIntent {
    object RetryLoadWeather : WeatherIntent()
}
