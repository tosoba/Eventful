package com.example.repo

import com.example.core.model.Resource
import com.example.core.model.weather.Forecast
import com.example.core.repo.IWeatherRepository
import com.example.core.util.ext.toResource
import com.example.weatherapi.model.DarkSkyApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(private val api: DarkSkyApi) : IWeatherRepository {

    override suspend fun getForecast(lat: Double, lon: Double): Resource<Forecast> = api
        .getForecastAsync(lat, lon)
        .await()
        .toResource()

    override suspend fun getForecastTimed(
        lat: Double,
        lon: Double,
        secondsSinceEpoch: Long
    ): Resource<Forecast> = api
        .getForecastTimedAsync(lat, lon, secondsSinceEpoch)
        .await()
        .toResource()
}
