package com.example.weather

import com.example.core.model.weather.Forecast

data class WeatherState(val forecastState: ForecastState) {
    companion object {
        val INITIAL = WeatherState(ForecastState.Initial)
    }
}

sealed class ForecastState {
    object Initial : ForecastState()
    object Loading : ForecastState()
    class Error(val throwable: Throwable?) : ForecastState()
    class Found(val forecast: Forecast) : ForecastState()
}