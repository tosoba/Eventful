package com.eventful.event.alarms

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class EventAlarmsViewModelProcessor(val value: String = "EventAlarmsViewModelProcessor")
