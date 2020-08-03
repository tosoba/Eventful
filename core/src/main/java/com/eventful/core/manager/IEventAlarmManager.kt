package com.eventful.core.manager

interface IEventAlarmManager {
    fun create(id: Int, timestamp: Long)
    fun cancel(id: Int)
    fun update(id: Int, timestamp: Long)
}
