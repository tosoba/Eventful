package com.example.core.model.weather

import com.google.gson.annotations.SerializedName

data class Hourly(
    @SerializedName("data") val `data`: List<HourlyData>,
    val icon: String,
    val summary: String
)