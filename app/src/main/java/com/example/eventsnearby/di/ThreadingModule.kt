package com.example.eventsnearby.di

import android.os.Handler
import android.os.HandlerThread
import com.example.coreandroid.di.Dependencies
import com.example.coreandroid.util.EpoxyThreads
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
class ThreadingModule {
    @Provides
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun epoxyThreads(): EpoxyThreads = EpoxyThreads(
        builder = Handler(
            HandlerThread(Dependencies.EPOXY_BUILDING_THREAD)
                .apply(HandlerThread::start).looper
        ),
        differ = Handler(
            HandlerThread(Dependencies.EPOXY_DIFFING_THREAD)
                .apply(HandlerThread::start).looper
        )
    )
}