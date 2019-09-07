package com.example.core

import java.io.IOException

sealed class NetworkResponse<out T : Any, out E : Any> {
    data class Success<T : Any>(val body: T) : NetworkResponse<T, Nothing>()
    data class ServerError<E : Any>(val body: E?, val code: Int? = null) :
        NetworkResponse<Nothing, E>()

    data class NetworkError(val error: IOException) : NetworkResponse<Nothing, Nothing>()
}
