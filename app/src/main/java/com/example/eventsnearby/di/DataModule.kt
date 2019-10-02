package com.example.eventsnearby.di

import com.example.core.repo.IEventsRepository
import com.example.core.repo.IWeatherRepository
import com.example.db.dao.EventDao
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

@Module //TODO: move this into the repo module
class DataModule {

    @Provides
    @Singleton
    fun eventsRepository(
        ticketMasterApi: TicketMasterApi,
        searchSuggestionDao: SearchSuggestionDao,
        eventDao: EventDao
    ): IEventsRepository = EventsRepository(ticketMasterApi, searchSuggestionDao, eventDao)

    @Provides
    @Singleton
    fun weatherRepository(api: DarkSkyApi): IWeatherRepository = WeatherRepository(api)

    @Provides
    @Singleton
    fun flickr(): Flickr = Flickr("788264798ee17aeec322c9930934dcd9", "08cd341dd13fba62", REST())
}