package com.example.core.model.app

data class LocationState(
    val latLng: LatLng? = null,
    val status: LocationStatus = LocationStatus.Initial
)
