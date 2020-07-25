package com.example.core.util.ext

import com.example.core.model.Resource
import com.haroldadmin.cnradapter.NetworkResponse

fun <T : Any, E : Any> NetworkResponse<T, E>.toResource(): Resource<T> = when (this) {
    is NetworkResponse.Success -> Resource.Success(body)
    is NetworkResponse.ServerError -> Resource.Error(body)
    is NetworkResponse.NetworkError -> Resource.Error(error)
}
