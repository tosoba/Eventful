package com.example.eventsapi

import com.example.eventsapi.model.EventsAuth
import com.example.eventsapi.model.EventsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface EventsApi {
    //TODO: start date with settings?
    @GET("events")
    @Headers(
        "Accept: application/json",
        "Authorization: Bearer ${EventsAuth.ACCESS_TOKEN}"
    )
    fun loadNearbyEvents(
        @Query("within") withinString: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("sort") sort: String? = null
    ): Call<EventsResponse>

    companion object {
        const val BASE_URL = "https://api.predicthq.com/v1/"
    }
}