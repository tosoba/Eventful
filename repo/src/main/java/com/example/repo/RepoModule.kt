package com.example.repo

import android.content.Context
import com.example.core.repo.IAppRepository
import com.example.core.repo.IEventsRepository
import com.example.core.repo.IWeatherRepository
import com.example.db.dao.EventDao
import com.example.db.dao.SearchSuggestionDao
import com.example.ticketmasterapi.TicketMasterApi
import com.example.weatherapi.model.DarkSkyApi
import com.patloew.rxlocation.RxLocation
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepoModule {

    @Provides
    @Singleton
    fun appRepository(
        appContext: Context,
        rxLocation: RxLocation
    ): IAppRepository = AppRepository(appContext, rxLocation)

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
}