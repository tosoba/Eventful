package com.example.weatherapi.model

import com.example.core.retrofit.retrofitWith
import com.haroldadmin.cnradapter.CoroutinesNetworkResponseAdapterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DarkSkyApiModule {

    @Provides
    @Singleton
    fun darkSkyApi(): DarkSkyApi = retrofitWith(
        url = DarkSkyApi.BASE_URL,
        callAdapters = listOf(CoroutinesNetworkResponseAdapterFactory())
    ).create(DarkSkyApi::class.java)
}