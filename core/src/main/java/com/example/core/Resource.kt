package com.example.core

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error<out T, out E>(val error: E, val data: T? = null) : Resource<T>()

    fun <S> map(mapper: (T) -> S): Resource<S> = when (this) {
        is Success<T> -> Success(mapper(data))
        is Error<T, *> -> Error(error, data?.let { mapper(it) })
    }
}
