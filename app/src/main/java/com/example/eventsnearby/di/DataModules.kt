package com.example.eventsnearby.di

import com.example.api.EventsApi
import com.example.core.IEventsRepository
import com.example.repo.EventsRepository
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val eventsDataModule = module {
    single {
        Retrofit.Builder()
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(EventsApi.BASE_URL)
            .build()
    }

    single {
        get<Retrofit>().create(EventsApi::class.java)
    }

    single<IEventsRepository> {
        EventsRepository(get())
    }
}