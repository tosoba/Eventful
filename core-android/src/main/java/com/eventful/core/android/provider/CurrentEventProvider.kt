package com.eventful.core.android.provider

import com.eventful.core.android.model.event.Event
import kotlinx.coroutines.flow.Flow

interface CurrentEventProvider {
    val event: Flow<Event>
}
