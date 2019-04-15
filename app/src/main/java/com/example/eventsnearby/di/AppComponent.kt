package com.example.eventsnearby.di

import com.example.eventsnearby.EventfulApp
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [UiModule::class, AndroidSupportInjectionModule::class])
interface AppComponent : AndroidInjector<EventfulApp> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<EventfulApp>()
}