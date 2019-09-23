package com.example.nearby

import com.example.coreandroid.ticketmaster.Event

sealed class NearbyViewAction

data class InvalidateList(val hideSnackbar: Boolean) : NearbyViewAction()

data class ShowEvent(val event: Event) : NearbyViewAction()

data class ShowSnackbarWithMsg(val msg: String) : NearbyViewAction()