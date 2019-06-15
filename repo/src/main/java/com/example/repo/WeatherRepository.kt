package com.example.repo

import com.example.core.IWeatherRepository
import com.example.core.Result
import com.example.core.model.weather.Forecast
import com.example.coreandroid.retrofit.awaitResult
import com.example.weatherapi.model.WeatherApi

class WeatherRepository(private val api: WeatherApi) : IWeatherRepository {

    override suspend fun getForecast(
        lat: Double, lon: Double
    ): Result<Forecast> = api.loadForecast(lat, lon).awaitResult()
}