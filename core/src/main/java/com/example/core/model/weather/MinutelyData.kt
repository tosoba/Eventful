package com.example.core.model.weather

data class MinutelyData(
    val precipIntensity: Double,
    val precipIntensityError: Double,
    val precipProbability: Double,
    val precipType: String,
    val time: Int
)