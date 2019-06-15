package com.example.core.model.weather

data class Currently(
    val apparentTemperature: Double,
    val cloudCover: Double,
    val dewPoint: Double,
    val humidity: Double,
    val icon: String,
    val nearestStormDistance: Int,
    val ozone: Double,
    val precipIntensity: Double,
    val precipIntensityError: Double,
    val precipProbability: Double,
    val precipType: String,
    val pressure: Double,
    val summary: String,
    val temperature: Double,
    val time: Int,
    val uvIndex: Int,
    val visibility: Double,
    val windBearing: Int,
    val windGust: Double,
    val windSpeed: Double
)