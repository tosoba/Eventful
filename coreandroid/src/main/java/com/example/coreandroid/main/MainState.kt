package com.example.coreandroid.main

//TODO: consider storing userLatLng here...
data class MainState(val isConnected: Boolean) {
    companion object {
        val INITIAL = MainState(false)
    }
}