package com.example.eventsnearby.di

import com.example.api.EventsApi
import com.example.core.IEventsRepository
import com.example.repo.EventsRepository
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
class EventsDataModule {

    @Provides
    @Singleton
    fun eventsRetrofit(): Retrofit = Retrofit.Builder()
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(EventsApi.BASE_URL)
        .build()

    @Provides
    @Singleton
    fun eventsApi(retrofit: Retrofit): EventsApi = retrofit.create(EventsApi::class.java)

    @Provides
    @Singleton
    fun eventsRepository(eventsApi: EventsApi): IEventsRepository = EventsRepository(eventsApi)
}