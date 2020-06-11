package com.example.core.usecase

import com.example.core.model.Resource
import com.example.core.model.weather.Forecast
import com.example.core.repo.IWeatherRepository
import javax.inject.Inject

class GetForecast @Inject constructor(private val repo: IWeatherRepository) {
    suspend operator fun invoke(
        lat: Double,
        lon: Double
    ): Resource<Forecast> = repo.getForecast(lat, lon)
}
