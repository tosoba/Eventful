package com.example.eventsnearby.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module

@Module
abstract class AppModule {

    @Binds
    abstract fun applicationContext(application: Application): Context
}