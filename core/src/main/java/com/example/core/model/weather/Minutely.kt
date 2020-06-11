package com.example.core.model.weather

import com.google.gson.annotations.SerializedName

data class Minutely(
    @SerializedName("data") val `data`: List<MinutelyData>,
    val icon: String,
    val summary: String
)
