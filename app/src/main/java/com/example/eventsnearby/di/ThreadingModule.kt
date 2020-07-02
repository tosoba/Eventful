package com.example.eventsnearby.di

import android.os.Handler
import android.os.HandlerThread
import com.example.coreandroid.view.epoxy.EpoxyThreads
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
            HandlerThread(EpoxyThreads.Names.BUILDING.value)
                .apply(HandlerThread::start).looper
        ),
        differ = Handler(
            HandlerThread(EpoxyThreads.Names.DIFFING.value)
                .apply(HandlerThread::start).looper
        )
    )
}
