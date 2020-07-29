package com.eventful.event

import com.eventful.core.util.Data
import com.eventful.core.android.model.event.Event

data class EventState(val event: Event, val isFavourite: Data<Boolean?> = Data(null))
