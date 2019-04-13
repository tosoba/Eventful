package com.example.nearby

sealed class NearbyError : Throwable(null, null) {
    object NotConnected : NearbyError()
    object LocationUnavailable : NearbyError()
}