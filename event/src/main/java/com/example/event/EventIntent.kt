package com.example.event

sealed class EventIntent {
    object ToggleFavourite : EventIntent()
}
