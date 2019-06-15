package com.example.weatherapi.model

import com.example.core.model.weather.Forecast
import com.example.core.model.weather.WeatherAuth
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface WeatherApi {
    @GET("forecast/{key}/{latitude},{longitude}")
    fun loadForecast(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Query("units") units: String = "si", //TODO: move this to settings
        @Query("lang") language: String = Locale.getDefault().language,
        @Query("exclude") exclude: String = "flags",
        @Path("key") key: String = WeatherAuth.KEY
    ): Call<Forecast>

    companion object {
        const val BASE_URL = "https://api.darksky.net/"
    }
}