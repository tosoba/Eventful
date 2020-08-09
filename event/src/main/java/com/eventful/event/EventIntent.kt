package com.eventful.event

import com.eventful.core.android.model.event.Event

sealed class EventIntent {
    data class NewEvent(val event: Event) : EventIntent()
    object BackPressed : EventIntent()
}
