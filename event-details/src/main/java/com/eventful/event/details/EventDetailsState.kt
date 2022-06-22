package com.eventful.event.details

import com.eventful.core.android.model.event.Event
import com.eventful.core.util.Data

data class EventDetailsState(val event: Event, val isFavourite: Data<Boolean?> = Data(null))
