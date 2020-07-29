package com.eventful.event

sealed class EventIntent {
    object ToggleFavourite : EventIntent()
}
