package com.eventful.weather

sealed class WeatherIntent {
    data class TabSelected(val tab: WeatherTab) : WeatherIntent()
    object RetryLoadWeather : WeatherIntent()
}
