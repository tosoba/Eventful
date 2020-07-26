package com.example.core.model.alarm

import com.example.core.model.event.IEvent

interface IAlarm {
    val id: Long
    val event: IEvent
    val timestamp: Long
}
