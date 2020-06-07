package com.example.weather

import com.example.core.model.weather.Forecast
import com.example.core.util.Data

data class WeatherState(val forecast: Data<Forecast?> = Data(
    null
)
)
