package com.example.event

import com.example.coreandroid.model.Event
import com.example.core.util.Data

data class EventState(val event: Event, val isFavourite: Data<Boolean>)
