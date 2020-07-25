package com.example.core.util

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun retrofitWith(
    url: String,
    client: OkHttpClient,
    converterFactory: Converter.Factory = GsonConverterFactory.create(),
    callAdapters: List<CallAdapter.Factory>? = null
): Retrofit = Retrofit.Builder()
    .client(client)
    .addConverterFactory(converterFactory)
    .apply {
        callAdapters?.forEach { addCallAdapterFactory(it) }
    }
    .baseUrl(url)
    .build()

fun onlineCacheInterceptor(maxAge: Long = 60 * 5): Interceptor = Interceptor { chain ->
    val response = chain.proceed(chain.request())
    response.newBuilder()
        .header("Cache-Control", "public, max-age=$maxAge")
        .removeHeader("Pragma")
        .build()
}

fun offlineCacheInterceptor(
    maxStale: Long = 60 * 60 * 24 * 7,
    isConnected: () -> Boolean
): Interceptor = Interceptor { chain ->
    val request = chain.request().run {
        if (!isConnected()) newBuilder()
            .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
            .removeHeader("Pragma")
            .build()
        else this
    }
    chain.proceed(request)
}
