package com.example.weatherapi.model

import com.example.core.util.retrofitWith
import com.haroldadmin.cnradapter.CoroutinesNetworkResponseAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
class DarkSkyApiModule {

    @Provides
    @Singleton
    fun darkSkyApi(client: OkHttpClient): DarkSkyApi = retrofitWith(
        client = client,
        url = DarkSkyApi.BASE_URL,
        callAdapters = listOf(CoroutinesNetworkResponseAdapterFactory())
    ).create(DarkSkyApi::class.java)
}
