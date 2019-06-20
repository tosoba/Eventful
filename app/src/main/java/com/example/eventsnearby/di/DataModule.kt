package com.example.eventsnearby.di

import com.example.core.IEventsRepository
import com.example.core.IWeatherRepository
import com.example.coreandroid.retrofit.retrofitWith
import com.example.eventsapi.EventsApi
import com.example.repo.EventsRepository
import com.example.repo.WeatherRepository
import com.example.weatherapi.model.WeatherApi
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    @Named(EVENTS_RETROFIT)
    fun eventsRetrofit(): Retrofit = retrofitWith(url = EventsApi.BASE_URL)

    @Provides
    @Singleton
    fun eventsApi(
        @Named(EVENTS_RETROFIT) retrofit: Retrofit
    ): EventsApi = retrofit.create(EventsApi::class.java)

    @Provides
    @Singleton
    fun eventsRepository(api: EventsApi): IEventsRepository = EventsRepository(api)

    @Provides
    @Singleton
    @Named(WEATHER_RETROFIT)
    fun weatherRetrofit(): Retrofit = retrofitWith(url = WeatherApi.BASE_URL)

    @Provides
    @Singleton
    fun weatherApi(
        @Named(WEATHER_RETROFIT) retrofit: Retrofit
    ): WeatherApi = retrofit.create(WeatherApi::class.java)

    @Provides
    @Singleton
    fun weatherRepository(api: WeatherApi): IWeatherRepository = WeatherRepository(api)

    @Provides
    @Singleton
    fun flickr(): Flickr = Flickr("13137d1d1b9498ebba88a45d22fe2c89", "5b5071455ab453f8", REST())

    companion object {
        private const val EVENTS_RETROFIT = "EVENTS_RETROFIT"
        private const val WEATHER_RETROFIT = "WEATHER_RETROFIT"
    }
}