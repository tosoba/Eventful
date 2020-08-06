package com.eventful.di

import android.content.Context
import android.content.Intent
import com.eventful.EventfulApp
import com.eventful.MainActivity
import com.eventful.core.android.di.name.MainActivityIntent
import com.eventful.core.android.manager.EventAlarmManager
import com.eventful.core.android.util.ext.isConnected
import com.eventful.core.manager.IEventAlarmManager
import com.eventful.core.util.offlineCacheInterceptor
import com.eventful.core.util.onlineCacheInterceptor
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.patloew.rxlocation.RxLocation
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import okhttp3.Cache
import okhttp3.OkHttpClient
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Module
abstract class AppModule {

    @Binds
    abstract fun applicationContext(application: EventfulApp): Context

    @Binds
    abstract fun eventAlarmManager(manager: EventAlarmManager): IEventAlarmManager

    companion object {

        @Provides
        @Singleton
        fun rxLocation(context: Context): RxLocation = RxLocation(context)

        @Provides
        @Singleton
        fun flickr(): Flickr = Flickr(
            "788264798ee17aeec322c9930934dcd9", "08cd341dd13fba62", REST()
        )

        @Provides
        @Singleton
        fun okHttpClient(context: Context): OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(onlineCacheInterceptor())
            .addInterceptor(offlineCacheInterceptor { context.isConnected })
            .cache(Cache(context.cacheDir, 10 * 1000 * 1000))
            .build()

        @Provides
        @MainActivityIntent
        fun mainActivityIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }
}
