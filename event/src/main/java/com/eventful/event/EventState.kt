package com.eventful.event

import com.eventful.core.android.model.event.Event

data class EventState(val events: List<Event>) {
    constructor(event: Event) : this(listOf(event))
}
