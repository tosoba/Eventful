package com.example.coreandroid.retrofit

import com.example.core.Failure
import com.example.core.Mappable
import com.example.core.Result
import com.example.core.Success
import com.example.coreandroid.BuildConfig
import com.google.gson.JsonParseException
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private val serverErrorCodes = 500..600
private val authenticationErrorCodes = 400..499

private const val DEFAULT_RETRY_ATTEMPTS = 3
private const val REPEAT_DELAY = 5000L

private fun mapError(error: Throwable?): Throwable? = when {
    error is JsonParseException -> ApiDataTransformationException
    error is IOException -> NetworkException
    error is ConnectException -> NetworkException
    error is HttpException && error.code() in serverErrorCodes -> ServerError
    error is HttpException && error.code() in authenticationErrorCodes -> AuthenticationError
    else -> error
}

private fun <T : Any> makeDataProvider(callWrapper: () -> Result<T>?): suspend () -> Result<T> = {
    suspendCoroutine { continuation ->
        val data = callWrapper()
        data?.run { continuation.resume(this) }
    }
}

private fun <T : Any> makeDataInvalidator(): (Result<T>) -> Boolean = { data ->
    data is Failure && (data.error == NetworkException || data.error == ServerError)
}

private suspend fun <T : Any> attemptProvideData(
    dataProvider: suspend () -> Result<T>,
    dataInvalidator: (Result<T>) -> Boolean,
    retryTimes: Int = DEFAULT_RETRY_ATTEMPTS,
    repeatDelay: Long = REPEAT_DELAY
): Result<T> {
    repeat(retryTimes - 1) {
        val data = dataProvider()
        if (!dataInvalidator(data)) return data
        delay(repeatDelay)
    }

    return dataProvider() //final attempt
}

private fun <T : Any> Response<T>.toFailure(): Failure? = errorBody()?.run {
    Failure(mapError(HttpException(this@toFailure)))
}

private fun <T : Any> Response<T>.toSuccess(): Success<T>? = body()?.run { Success(this) }

private fun <T : Mappable<R>, R : Any> Response<T>.toMappableSuccess(): Result<R>? = body()?.run {
    if (isValid) Success(data)
    else Failure(mapError(HttpException(this@toMappableSuccess)))
}

suspend fun <T : Any> Call<T>.awaitResult(): Result<T> {
    val callWrapper: () -> Result<T>? = {
        val call = clone()
        try {
            val response = call.execute()
            val result = response.toSuccess()
            val errorResult = response.toFailure()
            result ?: errorResult
        } catch (error: Throwable) {
            if (BuildConfig.DEBUG) error.printStackTrace()
            Failure(mapError(error))
        }
    }

    return attemptProvideData(
        dataProvider = makeDataProvider(callWrapper),
        dataInvalidator = makeDataInvalidator()
    )
}

suspend fun <T : Mappable<R>, R : Any> Call<T>.awaitMappableResult(): Result<R> {
    val callWrapper: () -> Result<R>? = {
        val call = clone()
        try {
            val response = call.execute()
            val result = response.toMappableSuccess()
            val errorResult = response.toFailure()
            result ?: errorResult
        } catch (error: Throwable) {
            if (BuildConfig.DEBUG) error.printStackTrace()
            Failure(mapError(error))
        }
    }

    return attemptProvideData(
        dataProvider = makeDataProvider(callWrapper),
        dataInvalidator = makeDataInvalidator()
    )
}

