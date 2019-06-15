package com.example.core.model.weather

data class Forecast(
    val alerts: List<Alert>,
    val currently: Currently,
    val daily: Daily,
    val hourly: Hourly,
    val latitude: Double,
    val longitude: Double,
    val minutely: Minutely,
    val timezone: String
)