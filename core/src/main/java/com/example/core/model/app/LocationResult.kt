package com.example.core.model.app

sealed class LocationResult {
    object Disabled : LocationResult()
    class Error(val throwable: Throwable) : LocationResult()
    class Found(val latLng: LatLng) : LocationResult()
}

class FailedToFindLocationException : Throwable()

sealed class LocationStatus {
    object Unknown : LocationStatus()
    object PermissionDenied : LocationStatus()
    object Disabled : LocationStatus()
    object Loading : LocationStatus()
    class Error(val throwable: Throwable) : LocationStatus()
    object Found : LocationStatus()
}

data class LocationState(
    val latLng: LatLng? = null,
    val status: LocationStatus = LocationStatus.Unknown
)