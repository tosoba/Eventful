package com.example.ticketmasterapi

import com.example.core.retrofit.retrofitWith
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TicketMasterApiModule {

    @Provides
    @Singleton
    fun ticketMasterApi(): TicketMasterApi = retrofitWith(TicketMasterApi.BASE_URL)
        .create(TicketMasterApi::class.java)
}