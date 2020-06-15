package com.example.event

import com.example.core.util.Data
import com.example.coreandroid.model.event.Event

data class EventState(val event: Event, val isFavourite: Data<Boolean>)
