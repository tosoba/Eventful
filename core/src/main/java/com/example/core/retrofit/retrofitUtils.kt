package com.example.core.retrofit

import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun retrofitWith(
    url: String,
    client: OkHttpClient = OkHttpClient(),
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