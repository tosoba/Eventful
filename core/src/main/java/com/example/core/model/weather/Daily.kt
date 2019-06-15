package com.example.core.model.weather

import com.google.gson.annotations.SerializedName

data class Daily(
    @SerializedName("data") val `data`: List<DailyData>,
    val icon: String,
    val summary: String
)