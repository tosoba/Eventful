package com.example.core.model.app

sealed class LocationResult {
    object Disabled : LocationResult()
    object Loading : LocationResult()
    class Error(val throwable: Throwable) : LocationResult()
    class Found(val latLng: LatLng) : LocationResult()
}