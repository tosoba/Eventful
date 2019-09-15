package com.example.nearby

import com.example.coreandroid.ticketmaster.Event

sealed class NearbyViewAction

data class UpdateEvents(val events: Collection<Event>) : NearbyViewAction()

data class ShowEvent(val event: Event) : NearbyViewAction()

object ShowNoConnectionMessage : NearbyViewAction()

object ShowLocationUnavailableMessage : NearbyViewAction()

object ShowLoadingSnackbar : NearbyViewAction()