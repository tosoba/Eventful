package com.eventful.di

import android.os.Handler
import android.os.HandlerThread
import com.eventful.core.android.view.epoxy.EpoxyThreads
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
object ThreadingModule {
    @Provides fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun epoxyThreads(): EpoxyThreads =
        EpoxyThreads(
            builder =
                Handler(
                    HandlerThread(EpoxyThreads.Names.BUILDING.value)
                        .apply(HandlerThread::start)
                        .looper),
            differ =
                Handler(
                    HandlerThread(EpoxyThreads.Names.DIFFING.value)
                        .apply(HandlerThread::start)
                        .looper))
}
