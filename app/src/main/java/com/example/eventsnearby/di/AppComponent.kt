package com.example.eventsnearby.di

import android.app.Application
import com.example.eventsnearby.EventfulApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        UiModule::class,
        DispatcherModule::class,
        AppModule::class,
        EventsDataModule::class,
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