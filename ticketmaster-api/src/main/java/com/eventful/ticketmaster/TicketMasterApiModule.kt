package com.eventful.ticketmaster

import com.eventful.core.util.retrofitWith
import com.haroldadmin.cnradapter.CoroutinesNetworkResponseAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
object TicketMasterApiModule {
    @Provides
    @Singleton
    fun ticketMasterApi(client: OkHttpClient): TicketMasterApi = retrofitWith(
        client = client,
        url = TicketMasterApi.BASE_URL,
        callAdapters = listOf(CoroutinesNetworkResponseAdapterFactory())
    ).create(TicketMasterApi::class.java)
}
