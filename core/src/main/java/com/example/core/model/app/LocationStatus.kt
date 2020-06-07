package com.example.core.model.app

sealed class LocationStatus {
    object Initial : LocationStatus()
    object PermissionDenied : LocationStatus()
    object Disabled : LocationStatus()
    object Loading : LocationStatus()
    class Error(val throwable: Throwable) : LocationStatus()
    object Found : LocationStatus()
}