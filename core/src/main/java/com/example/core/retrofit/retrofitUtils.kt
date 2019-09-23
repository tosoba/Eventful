package com.example.core.retrofit

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

sealed class RetryStrategy(val attempts: Int)
class Times(attempts: Int = 1) : RetryStrategy(attempts)
class WithDelay(val delay: Long, val unit: TimeUnit, attempts: Int = 1) : RetryStrategy(attempts)
class WithVariableDelay(
    attempts: Int, val unit: TimeUnit, val getDelay: (Int) -> Long
) : RetryStrategy(attempts)

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

fun onlineCacheInterceptor(maxAge: Long = 60 * 5) = Interceptor { chain ->
    val response = chain.proceed(chain.request())
    response.newBuilder()
        .header("Cache-Control", "public, max-age=$maxAge")
        .removeHeader("Pragma")
        .build()
}

fun offlineCacheInterceptor(
    maxStale: Long = 60 * 60 * 24 * 7,
    isConnected: () -> Boolean
) = Interceptor { chain ->
    val request = chain.request().run {
        if (!isConnected()) newBuilder()
            .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
            .removeHeader("Pragma")
            .build()
        else this
    }
    chain.proceed(request)
}