package com.eventful.darsky.model

import com.eventful.core.model.weather.Forecast
import com.eventful.core.model.weather.WeatherAuth
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface DarkSkyApi {
    @GET("forecast/{key}/{latitude},{longitude}")
    fun getForecastAsync(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Path("key") key: String = WeatherAuth.KEY,
        @Query("units") units: String = "si",
        @Query("lang") language: String = Locale.getDefault().language,
        @Query("exclude") exclude: String = "flags"
    ): Deferred<NetworkResponse<Forecast, DarkSkyError>>

    @GET("forecast/{key}/{latitude},{longitude},{time}")
    fun getForecastTimedAsync(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Path("time") secondsSinceEpoch: Long,
        @Path("key") key: String = WeatherAuth.KEY,
        @Query("units") units: String = "si",
        @Query("lang") language: String = Locale.getDefault().language,
        @Query("exclude") exclude: String = "flags"
    ): Deferred<NetworkResponse<Forecast, DarkSkyError>>

    companion object {
        const val BASE_URL = "https://api.darksky.net/"
    }
}
