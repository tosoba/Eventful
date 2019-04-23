package com.example.coreandroid.util

import com.google.android.gms.maps.model.LatLng

sealed class LocationState {
    object Unknown : LocationState()
    object PermissionDenied : LocationState()
    object Disabled : LocationState()
    object Loading : LocationState()
    class Error(val throwable: Throwable) : LocationState()
    class Found(val latLng: LatLng) : LocationState()
}