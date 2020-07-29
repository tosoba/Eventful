package com.eventful.core.util.ext

import com.eventful.core.model.Resource
import com.haroldadmin.cnradapter.NetworkResponse

fun <T : Any, E : Any> NetworkResponse<T, E>.toResource(): Resource<T> = when (this) {
    is NetworkResponse.Success -> Resource.Success(body)
    is NetworkResponse.ServerError -> Resource.Error(body)
    is NetworkResponse.NetworkError -> Resource.Error(error)
}
