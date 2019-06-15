package com.example.core

import com.example.core.model.weather.Forecast

interface IWeatherRepository {
    suspend fun getForecast(lat: Double, lon: Double): Result<Forecast>
}