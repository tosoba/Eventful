package com.example.coreandroid.arch.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

open class BaseFeature {

    @ExperimentalCoroutinesApi
    protected suspend fun <S> ProducerScope<Action<S>>.send(action: S.() -> S) = send(
        StateTransition(action)
    )

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    protected suspend fun <T> ProducerScope<Action<T>>.sendAll(channel: ReceiveChannel<Action<T>>) {
        channel.consumeEach { originalAction ->
            send(originalAction)
        }
    }

    @ExperimentalCoroutinesApi
    protected fun <T> CoroutineScope.produceActions(
        actionsProducer: suspend ProducerScope<Action<T>>.() -> Unit
    ): ReceiveChannel<Action<T>> = produce(block = actionsProducer)

    fun cleanUp() = Unit
}