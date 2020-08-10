package com.eventful.repo

import com.eventful.core.model.Resource
import com.eventful.core.model.weather.Forecast
import com.eventful.core.repo.IWeatherRepository
import com.eventful.core.util.ext.toResource
import com.eventful.darsky.api.DarkSkyApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(private val api: DarkSkyApi) : IWeatherRepository {

    override suspend fun getForecast(lat: Double, lon: Double): Resource<Forecast> = api
        .getForecastAsync(lat, lon)
        .await()
        .toResource()

    override suspend fun getForecastTimed(
        lat: Double, lon: Double, secondsSinceEpoch: Long
    ): Resource<Forecast> = api
        .getForecastTimedAsync(lat, lon, secondsSinceEpoch)
        .await()
        .toResource()
}
