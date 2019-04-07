package com.example.coreandroid.arch.ui

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * It implements a Factory pattern generating Rx Subjects based on Event Types.
 * It maintain a map of Rx Subjects, one per type per instance of UIEventBusFactory.
 *
 * @param owner is a LifecycleOwner used to auto dispose based on destroy observable
 */
class UIEventBusFactory private constructor(val owner: LifecycleOwner) {

    companion object {

        private val buses = mutableMapOf<LifecycleOwner, UIEventBusFactory>()

        /**
         * Return the [UIEventBusFactory] associated to the [LifecycleOwner]. It there is no bus it will create one.
         * If the [LifecycleOwner] used is a fragment it use [Fragment#getViewLifecycleOwner()]
         */
        @JvmStatic
        fun get(
            lifecycleOwner: LifecycleOwner
        ): UIEventBusFactory = buses[lifecycleOwner] ?: run {
            val bus = UIEventBusFactory(lifecycleOwner)
            buses[lifecycleOwner] = bus
            lifecycleOwner.lifecycle.addObserver(bus.observer)
            bus
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val map = HashMap<Class<*>, Channel<*>>()

    internal val observer = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            map.forEach { entry -> entry.value.cancel() }
            buses.remove(owner)
        }
    }

    fun <T> create(clazz: Class<T>): Channel<T> {
        val subject = Channel<T>()
        map[clazz] = subject
        return subject
    }

    /**
     * emit will create (if needed) or use the existing Rx Subject to transition events.
     *
     * @param clazz is the Event Class
     * @param event is the instance of the Event to be sent
     */
    inline fun <reified T : UIComponentEvent> emit(event: T) {
        val subject = if (map[T::class.java] != null) map[T::class.java] else create(T::class.java)
        (subject as Channel<T>).offer(event)
    }

    /**x
     * getSafeManagedObservable returns an Rx Observable which is
     *  *Safe* against reentrant events as it is serialized and
     *  *Managed* since it disposes itself based on the lifecycle
     *
     *  @param clazz is the class of the event type used by this observable
     */
    fun <T : UIComponentEvent> getSafeManagedObservable(clazz: Class<T>): ReceiveChannel<T> {
        return if (map[clazz] != null) map[clazz] as ReceiveChannel<T> else create(clazz)
    }
}




