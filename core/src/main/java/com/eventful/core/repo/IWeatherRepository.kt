package com.eventful.core.repo

import com.eventful.core.model.Resource
import com.eventful.core.model.weather.Forecast

interface IWeatherRepository {
    suspend fun getForecast(lat: Double, lon: Double): Resource<Forecast>
    suspend fun getForecastTimed(
        lat: Double,
        lon: Double,
        secondsSinceEpoch: Long
    ): Resource<Forecast>
}
