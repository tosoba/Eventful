package com.example.eventsnearby

import com.example.coreandroid.util.SnackbarState

//TODO: consider storing userLatLng here...
data class MainState(val isConnected: Boolean, val snackbarState: SnackbarState) {
    companion object {
        val INITIAL = MainState(false, SnackbarState.Hidden)
    }
}