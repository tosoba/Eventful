package com.eventful.core.android.util.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver

operator fun Lifecycle.plusAssign(observer: LifecycleObserver) = addObserver(observer)

operator fun Lifecycle.plusAssign(observers: Collection<LifecycleObserver>) = observers.forEach { addObserver(it) }
