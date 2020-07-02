package com.example.weather

sealed class WeatherIntent {
    object RetryLoadWeather : WeatherIntent()
}
