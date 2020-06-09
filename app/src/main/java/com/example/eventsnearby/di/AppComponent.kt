package com.example.eventsnearby.di

import com.example.db.DbModule
import com.example.eventsnearby.EventfulApp
import com.example.eventsnearby.MainActivityModule
import com.example.repo.RepoModule
import com.example.ticketmasterapi.TicketMasterApiModule
import com.example.weatherapi.model.DarkSkyApiModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
@Component(
    modules = [
        ThreadingModule::class,
        AppModule::class,
        TicketMasterApiModule::class,
        DarkSkyApiModule::class,
        DbModule::class,
        RepoModule::class,
        AndroidSupportInjectionModule::class,
        MainActivityModule::class
    ]
)
interface AppComponent : AndroidInjector<EventfulApp> {
    @Component.Factory
    abstract class Factory : AndroidInjector.Factory<EventfulApp>
}