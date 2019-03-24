package com.example.coreandroid.arch.ui.ext

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.coreandroid.arch.ui.UIComponentEvent
import com.example.coreandroid.arch.ui.UIEventBusFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel

val LifecycleOwner.untilDestroy: Job
    get() = Job().apply {
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() = cancel()
        })
    }

/**
 * Extension on [LifecycleOwner] used to emit an event.
 */
inline fun <reified T : UIComponentEvent> LifecycleOwner.emit(event: T) = with(UIEventBusFactory.get(this)) {
    getSafeManagedObservable(T::class.java)
    emit(event)
}

/**
 * Extension on [LifecycleOwner] used used to get the state observable.
 */
inline fun <reified T : UIComponentEvent> LifecycleOwner.getSafeManagedObservable(): ReceiveChannel<T> = UIEventBusFactory
    .get(this)
    .getSafeManagedObservable(T::class.java)