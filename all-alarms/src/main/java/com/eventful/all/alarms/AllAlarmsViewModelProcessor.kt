package com.eventful.all.alarms

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AllAlarmsViewModelProcessor(val value: String = "AllAlarmsViewModelProcessor")
