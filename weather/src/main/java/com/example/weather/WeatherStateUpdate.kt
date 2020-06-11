package com.example.weather

import com.example.core.model.Resource
import com.example.core.model.weather.Forecast
import com.example.coreandroid.util.StateUpdate

sealed class WeatherStateUpdate :
    StateUpdate<WeatherState> {
    sealed class Weather : WeatherStateUpdate() {
        object Loading : Weather() {
            override fun invoke(state: WeatherState): WeatherState = state.copy(
                forecast = state.forecast.copyWithLoadingStatus
            )
        }

        class Loaded(private val resource: Resource<Forecast>) : Weather() {
            override fun invoke(state: WeatherState): WeatherState = when (resource) {
                is Resource.Success -> state.copy(
                    forecast = state.forecast.copyWithNewValue(resource.data)
                )
                is Resource.Error<Forecast> -> state.copy(
                    forecast = state.forecast.copyWithFailureStatus(resource.error)
                )
            }
        }
    }
}
