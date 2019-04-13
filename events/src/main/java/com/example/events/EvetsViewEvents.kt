package com.example.events

import com.example.coreandroid.model.EventUiModel

sealed class EventsViewEvent

data class EventClicked(val event: EventUiModel) : EventsViewEvent()

object EventListScrolledToEnd : EventsViewEvent()