package com.example.ticketmasterapi

import com.example.ticketmasterapi.model.EventSearchResponse
import com.example.ticketmasterapi.queryparam.GeoPoint
import com.example.ticketmasterapi.queryparam.RadiusUnit
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TicketMasterApi {
    @GET("events")
    fun searchEvents(
        @Query("id") id: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("attractionId") attractionId: String? = null,
        @Query("venueId") venueId: String? = null,
        @Query("postalCode") postalCode: String? = null,
        @Query("radius") radius: Float? = null,
        @Query("unit") radiusUnit: RadiusUnit? = null,
        @Query("size") size: Int = 20,
        @Query("page") page: Int = 0,
        @Query("geoPoint") geoPoint: GeoPoint? = null
    ): Call<EventSearchResponse>
}