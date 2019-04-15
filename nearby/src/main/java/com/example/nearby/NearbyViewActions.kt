package com.example.nearby

import com.example.coreandroid.model.EventUiModel

sealed class NearbyViewAction

data class UpdateEvents(val events: List<EventUiModel>) : NearbyViewAction()

data class ShowEvent(val event: EventUiModel) : NearbyViewAction()

object ShowNoConnectionMessage : NearbyViewAction()

object ShowLocationUnavailableMessage : NearbyViewAction()

object ShowLoadingSnackbar : NearbyViewAction()