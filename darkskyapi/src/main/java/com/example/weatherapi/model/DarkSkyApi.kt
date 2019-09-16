package com.example.weatherapi.model

import com.example.core.model.weather.Forecast
import com.example.core.model.weather.WeatherAuth
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface DarkSkyApi {
    @GET("forecast/{key}/{latitude},{longitude}")
    fun loadForecast(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Path("key") key: String = WeatherAuth.KEY,
        @Query("units") units: String = "si", //TODO: move this to settings
        @Query("lang") language: String = Locale.getDefault().language,
        @Query("exclude") exclude: String = "flags"
    ): Deferred<NetworkResponse<Forecast, DarkSkyError>>

    companion object {
        const val BASE_URL = "https://api.darksky.net/"
    }
}