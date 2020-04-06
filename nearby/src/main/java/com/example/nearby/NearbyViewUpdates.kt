package com.example.nearby

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.SnackbarState

sealed class NearbyViewUpdate

data class InvalidateList(val errorOccurred: Boolean) : NearbyViewUpdate()

data class ShowEvent(val event: Event) : NearbyViewUpdate()

data class FinishActionModeWithMsg(val msg: String) : NearbyViewUpdate()

data class UpdateSnackbar(val state: SnackbarState) : NearbyViewUpdate()
