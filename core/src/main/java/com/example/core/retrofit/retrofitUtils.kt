package com.example.core.retrofit

import com.example.core.NetworkResponse
import com.google.gson.Gson
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun <T : Any, E : Any> responseProvider(
    callWrapper: () -> NetworkResponse<T, E>?
): suspend () -> NetworkResponse<T, E> = {
    suspendCoroutine { continuation ->
        val data = callWrapper()
        data?.run { continuation.resume(this) }
    }
}

fun <T : Any, E : Any> makeResponseInvalidator(): (NetworkResponse<T, E>) -> Boolean = { response ->
    response is NetworkResponse.NetworkError || response is NetworkResponse.ServerError<*>
}

suspend fun <T : Any, E : Any> attemptCall(
    responseProvider: suspend () -> NetworkResponse<T, E>,
    responseInvalidator: (NetworkResponse<T, E>) -> Boolean,
    retryStrategy: RetryStrategy
): NetworkResponse<T, E> {
    repeat(retryStrategy.attempts - 1) {
        val data = responseProvider()
        if (!responseInvalidator(data)) return data

        when (retryStrategy) {
            is WithDelay -> delay(retryStrategy.unit.toMillis(retryStrategy.delay))
            is WithVariableDelay -> delay(retryStrategy.unit.toMillis(retryStrategy.getDelay(it)))
        }
    }

    return responseProvider() //final attempt
}

inline fun <T : Any, reified E : Any> Response<T>.toFailure(): NetworkResponse.ServerError<E>? =
    errorBody()?.run {
        NetworkResponse.ServerError(Gson().fromJson(string(), E::class.java))
    }

fun <T : Any> Response<T>.toSuccess(): NetworkResponse.Success<T>? = body()?.run {
    NetworkResponse.Success(this)
}

suspend inline fun <reified T : Any, reified E : Any> Call<T>.awaitResponse(
    retryStrategy: RetryStrategy? = null
): NetworkResponse<T, E> {
    val callWrapper: () -> NetworkResponse<T, E>? = {
        val call = clone()
        try {
            val response = call.execute()
            if (response.isSuccessful) response.toSuccess()
            else response.toFailure<T, E>()
        } catch (ex: IOException) {
            NetworkResponse.NetworkError(ex)
        }
    }

    return retryStrategy?.let {
        attemptCall(
            responseProvider = responseProvider(callWrapper),
            responseInvalidator = makeResponseInvalidator(),
            retryStrategy = it
        )
    } ?: responseProvider(callWrapper).invoke()
}

fun retrofitWith(
    url: String,
    client: OkHttpClient = OkHttpClient(),
    converterFactory: Converter.Factory = GsonConverterFactory.create()
): Retrofit = Retrofit.Builder()
    .client(client)
    .addConverterFactory(converterFactory)
    .baseUrl(url)
    .build()