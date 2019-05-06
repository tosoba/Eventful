package com.example.eventsnearby.di

import android.app.Application
import android.content.Context
import com.patloew.rxlocation.RxLocation
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.nlopez.smartlocation.SmartLocation
import javax.inject.Singleton

@Module(includes = [AppModule.Providers::class])
abstract class AppModule {

    @Binds
    abstract fun applicationContext(application: Application): Context

    @Module
    class Providers {
        @Provides
        @Singleton
        fun smartLocation(context: Context): SmartLocation = SmartLocation.with(context)

        @Provides
        @Singleton
        fun rxLocation(context: Context): RxLocation = RxLocation(context)
    }
}