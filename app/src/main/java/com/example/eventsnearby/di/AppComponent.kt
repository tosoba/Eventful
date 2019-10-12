package com.example.eventsnearby.di

import android.app.Application
import com.example.db.DbModule
import com.example.eventsnearby.EventfulApp
import com.example.repo.RepoModule
import com.example.ticketmasterapi.TicketMasterApiModule
import com.example.weatherapi.model.DarkSkyApiModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        UiModule::class,
        ThreadingModule::class,
        AppModule::class,
        NetworkModule::class,
        TicketMasterApiModule::class,
        DarkSkyApiModule::class,
        DbModule::class,
        RepoModule::class,
        AndroidSupportInjectionModule::class
    ]
)
interface AppComponent : AndroidInjector<EventfulApp> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<EventfulApp>() {
        @BindsInstance
        abstract fun application(application: Application): Builder
    }
}