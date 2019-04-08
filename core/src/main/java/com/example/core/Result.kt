package com.example.core

sealed class Result<out T : Any>

data class Success<out T : Any>(val data: T) : Result<T>()

data class Failure(val error: Throwable?) : Result<Nothing>()

inline fun <T : Any> Result<T>.doOnSuccess(consumer: (T) -> Unit): Result<T> {
    if (this is Success) consumer(data)
    return this
}

inline fun <T : Any> Result<T>.doOnError(consumer: (Throwable?) -> Unit) {
    if (this is Failure) consumer(error)
}

inline fun <T : Any> Result<T>.`do`(
    onSuccess: (T) -> Unit,
    onError: (Throwable?) -> Unit
) {
    when (this) {
        is Success -> onSuccess(data)
        is Failure -> onError(error)
    }
}

inline fun <T : Any, R : Any> Result<T>.mapSuccess(mapper: (T) -> R): Result<R> = when (this) {
    is Success -> Success(mapper(data))
    is Failure -> this
}

interface Mappable<out T : Any> {
    val isValid: Boolean
    val data: T
}
