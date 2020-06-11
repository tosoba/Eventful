package com.example.core.repo

import com.example.core.model.Resource
import com.example.core.model.weather.Forecast

interface IWeatherRepository {
    suspend fun getForecast(lat: Double, lon: Double): Resource<Forecast>
}
