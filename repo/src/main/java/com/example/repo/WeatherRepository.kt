package com.example.repo

import com.example.core.IWeatherRepository
import com.example.core.Resource
import com.example.core.model.weather.Forecast
import com.example.weatherapi.model.DarkSkyApi
import com.haroldadmin.cnradapter.NetworkResponse

class WeatherRepository(private val api: DarkSkyApi) : IWeatherRepository {

    override suspend fun getForecast(
        lat: Double, lon: Double
    ): Resource<Forecast> = when (val response = api.loadForecast(lat, lon).await()) {
        is NetworkResponse.Success -> Resource.Success(response.body)
        is NetworkResponse.ServerError -> Resource.Error(response.body)
        is NetworkResponse.NetworkError -> Resource.Error(response.error)
    }
}