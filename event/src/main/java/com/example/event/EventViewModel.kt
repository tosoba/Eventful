package com.example.event

import com.example.core.usecase.IsEventSaved
import com.haroldadmin.vector.VectorViewModel

class EventViewModel(
    private val isEventSaved: IsEventSaved
) : VectorViewModel<EventState>(EventState.INITIAL) {
}