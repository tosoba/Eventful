package com.eventful.core.model

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val error: Any? = null, val data: T? = null) : Resource<T>()

    fun <S> map(mapper: (T) -> S): Resource<S> =
        when (this) {
            is Success<T> -> Success(mapper(data))
            is Error<T> -> Error(error, data?.let { mapper(it) })
        }

    companion object {
        fun <T> successWith(data: T): Resource<T> = Success(data)
        fun <T> errorWith(error: Any? = null, data: T? = null): Resource<T> = Error(error, data)
    }
}
