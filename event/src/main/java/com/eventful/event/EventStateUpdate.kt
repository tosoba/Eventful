package com.eventful.event

import com.eventful.core.android.base.StateUpdate
import com.eventful.core.android.model.event.Event

sealed class EventStateUpdate : StateUpdate<EventState> {
    data class AddEvent(val event: Event) : EventStateUpdate() {
        override fun invoke(state: EventState): EventState =
            state.copy(events = state.events + event)
    }

    object DropLastEvent : EventStateUpdate() {
        override fun invoke(state: EventState): EventState =
            state.copy(events = state.events.dropLast(1))
    }
}
