package com.example.eventsnearby.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
class DispatcherModule {
    @Provides
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
}