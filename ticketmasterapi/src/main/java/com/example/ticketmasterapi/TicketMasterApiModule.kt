package com.example.ticketmasterapi

import com.example.core.retrofit.retrofitWith
import com.haroldadmin.cnradapter.CoroutinesNetworkResponseAdapterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class TicketMasterApiModule {

    @Provides
    @Singleton
    fun ticketMasterApi(): TicketMasterApi = retrofitWith(
        url = TicketMasterApi.BASE_URL,
        callAdapters = listOf(CoroutinesNetworkResponseAdapterFactory())
    ).create(TicketMasterApi::class.java)
}