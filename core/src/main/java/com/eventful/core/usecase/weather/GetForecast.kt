package com.eventful.core.usecase.weather

import com.eventful.core.model.Resource
import com.eventful.core.model.weather.Forecast
import com.eventful.core.repo.IWeatherRepository
import javax.inject.Inject

class GetForecast @Inject constructor(private val repo: IWeatherRepository) {
    suspend operator fun invoke(
        lat: Double,
        lon: Double
    ): Resource<Forecast> = repo.getForecast(lat, lon)
}
