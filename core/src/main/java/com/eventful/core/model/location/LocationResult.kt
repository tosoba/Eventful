package com.eventful.core.model.location

sealed class LocationResult {
    object Disabled : LocationResult()
    object Loading : LocationResult()
    class Error(val throwable: Throwable) : LocationResult()
    class Found(val latitude: Double, val longitude: Double) : LocationResult()
}
