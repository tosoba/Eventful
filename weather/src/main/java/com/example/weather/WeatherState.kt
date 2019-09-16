package com.example.weather

import com.example.core.model.weather.Forecast
import com.example.coreandroid.util.Data
import com.example.coreandroid.util.Initial
import com.haroldadmin.vector.VectorState

data class WeatherState(val forecast: Data<Forecast?>) : VectorState {
    companion object {
        val INITIAL = WeatherState(
            Data(
                null,
                Initial
            )
        )
    }
}
