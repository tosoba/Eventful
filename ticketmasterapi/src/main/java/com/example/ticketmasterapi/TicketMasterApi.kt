package com.example.ticketmasterapi

import com.example.ticketmasterapi.model.EventSearchResponse
import com.example.ticketmasterapi.model.TicketMasterErrorResponse
import com.example.ticketmasterapi.queryparam.GeoPoint
import com.example.ticketmasterapi.queryparam.RadiusUnit
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Deferred
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
        @Query("radius") radius: Int? = null,
        @Query("unit") radiusUnit: RadiusUnit? = null,
        @Query("size") size: Int = 20,
        @Query("page") page: Int? = null,
        @Query("geoPoint") geoPoint: GeoPoint? = null,
        @Query("apikey") apiKey: String = TicketMasterAuth.key
    ): Deferred<NetworkResponse<EventSearchResponse, TicketMasterErrorResponse>>

    companion object {
        const val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
    }
}
