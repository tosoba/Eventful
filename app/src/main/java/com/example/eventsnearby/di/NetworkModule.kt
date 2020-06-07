package com.example.eventsnearby.di

import android.content.Context
import com.example.core.util.offlineCacheInterceptor
import com.example.core.util.onlineCacheInterceptor
import com.example.coreandroid.di.Dependencies
import com.example.coreandroid.util.ext.isConnected
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    @Named(Dependencies.ONLINE_INTERCEPTOR)
    fun okHttpOnlineCacheInterceptor() = onlineCacheInterceptor()

    @Provides
    @Singleton
    @Named(Dependencies.OFFLINE_INTERCEPTOR)
    fun okHttpOfflineCacheInterceptor(context: Context) = offlineCacheInterceptor {
        context.isConnected
    }

    @Provides
    @Singleton
    fun cache(context: Context): Cache = Cache(context.cacheDir, 10 * 1000 * 1000)

    @Provides
    @Singleton
    fun okHttpClient(
        @Named(Dependencies.ONLINE_INTERCEPTOR) onlineInterceptor: Interceptor,
        @Named(Dependencies.OFFLINE_INTERCEPTOR) offlineInterceptor: Interceptor,
        cache: Cache
    ): OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(onlineInterceptor)
        .addInterceptor(offlineInterceptor)
        .cache(cache)
        .build()
}