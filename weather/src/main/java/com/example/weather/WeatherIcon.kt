package com.example.weather

import androidx.annotation.DrawableRes

enum class WeatherIcon(@DrawableRes val resource: Int, val iconName: String) {
    CLEAR_DAY(R.drawable.clear_day, "clear-day"),
    CLEAR_NIGHT(R.drawable.clear_night, "clear-night"),
    CLOUDY(R.drawable.cloudy, "cloudy"),
    FOG(R.drawable.fog, "fog"),
    PARTLY_CLOUDY_DAY(R.drawable.partly_cloudy_day, "partly-cloudy-day"),
    PARTLY_CLOUDY_NIGHT(R.drawable.partly_cloudy_night, "partly-cloudy-night"),
    RAIN(R.drawable.rain, "rain"),
    SLEET(R.drawable.sleet, "sleet"),
    SNOW(R.drawable.snow, "snow"),
    WIND(R.drawable.wind, "wind"),
    UNKNOWN(R.drawable.unknown, "unknown");

    companion object {
        fun fromName(name: String): WeatherIcon = values()
            .find { it.iconName.equals(name, true) } ?: UNKNOWN
    }
}