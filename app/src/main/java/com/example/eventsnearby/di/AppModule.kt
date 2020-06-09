package com.example.eventsnearby.di

import android.app.Application
import android.content.Context
import com.example.core.util.offlineCacheInterceptor
import com.example.core.util.onlineCacheInterceptor
import com.example.coreandroid.util.ext.isConnected
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.patloew.rxlocation.RxLocation
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module(includes = [AppModule.Providers::class])
abstract class AppModule {

    @Binds
    abstract fun applicationContext(application: Application): Context

    @Module
    class Providers {

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
    }
}