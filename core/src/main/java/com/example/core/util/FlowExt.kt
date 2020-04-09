package com.example.core.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalCoroutinesApi
fun <T, R> Flow<T>.flatMapFirst(
    transform: suspend (value: T) -> Flow<R>
): Flow<R> = map(transform).flattenFirst()

@ExperimentalCoroutinesApi
fun <T> Flow<Flow<T>>.flattenFirst(): Flow<T> = channelFlow {
    val busy = AtomicBoolean(false)
    collect { inner ->
        if (busy.compareAndSet(false, true)) {
            launch {
                try {
                    inner.collect { this@channelFlow.send(it) }
                    busy.set(false)
                } catch (e: CancellationException) {
                    this@channelFlow.cancel(e)
                }
            }
        }
    }
}