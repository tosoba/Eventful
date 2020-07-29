package com.eventful.core.model.alarm

import com.eventful.core.model.event.IEvent

interface IAlarm {
    val id: Long
    val event: IEvent
    val timestamp: Long
}
