package com.example.eventsnearby.di

import android.os.Handler
import android.os.HandlerThread
import com.example.coreandroid.di.Dependencies
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
class ThreadingModule {
    @Provides
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    @Named(Dependencies.EPOXY_DIFFING_THREAD)
    fun epoxyDiffingThread(): HandlerThread = HandlerThread(Dependencies.EPOXY_DIFFING_THREAD)
        .apply(HandlerThread::start)

    @Provides
    @Singleton
    @Named(Dependencies.EPOXY_MODEL_BUILDING_THREAD)
    fun epoxyModelBuildingThread(): HandlerThread =
        HandlerThread(Dependencies.EPOXY_MODEL_BUILDING_THREAD)
            .apply(HandlerThread::start)

    @Provides
    @Singleton
    @Named(Dependencies.EPOXY_DIFFER)
    fun epoxyDiffer(
        @Named(Dependencies.EPOXY_DIFFING_THREAD) thread: HandlerThread
    ): Handler = Handler(thread.looper)

    @Provides
    @Singleton
    @Named(Dependencies.EPOXY_BUILDER)
    fun epoxyBuilder(
        @Named(Dependencies.EPOXY_MODEL_BUILDING_THREAD) thread: HandlerThread
    ): Handler = Handler(thread.looper)
}