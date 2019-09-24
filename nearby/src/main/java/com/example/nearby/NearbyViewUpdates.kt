package com.example.nearby

import com.example.coreandroid.ticketmaster.Event

sealed class NearbyViewUpdate

data class InvalidateList(val hideSnackbar: Boolean) : NearbyViewUpdate()

data class ShowEvent(val event: Event) : NearbyViewUpdate()

data class ShowSnackbarAndInvalidateList(val msg: String, val errorOccurred: Boolean) :
    NearbyViewUpdate()
