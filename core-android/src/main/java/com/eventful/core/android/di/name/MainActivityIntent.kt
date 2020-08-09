package com.eventful.core.android.di.name

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class MainActivityIntent(val value: String = "MainActivityIntent")
