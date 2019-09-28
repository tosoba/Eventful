package com.example.eventsnearby.di

import com.example.core.repo.IEventsRepository
import com.example.core.repo.IWeatherRepository
import com.example.db.dao.SearchSuggestionDao
import com.example.repo.EventsRepository
import com.example.repo.WeatherRepository
import com.example.ticketmasterapi.TicketMasterApi
import com.example.weatherapi.model.DarkSkyApi
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    @Singleton
    fun eventsRepository(
        ticketMasterApi: TicketMasterApi,
        searchSuggestionDao: SearchSuggestionDao
    ): IEventsRepository = EventsRepository(ticketMasterApi, searchSuggestionDao)

    @Provides
    @Singleton
    fun weatherRepository(api: DarkSkyApi): IWeatherRepository = WeatherRepository(api)

    @Provides
    @Singleton
    fun flickr(): Flickr = Flickr("788264798ee17aeec322c9930934dcd9", "08cd341dd13fba62", REST())
}