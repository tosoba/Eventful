package com.eventful.event.details

sealed class EventDetailsIntent {
    object ToggleFavourite : EventDetailsIntent()
}
