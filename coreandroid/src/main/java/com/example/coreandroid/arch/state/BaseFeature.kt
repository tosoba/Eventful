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
    protected fun <T> CoroutineScope.actions(
        actionsProducer: suspend ProducerScope<Action<T>>.() -> Unit
    ): ReceiveChannel<Action<T>> = produce(block = actionsProducer)

    @ExperimentalCoroutinesApi
    protected inline fun <S> CoroutineScope.loadAsyncDataActions(
        crossinline load: suspend () -> S
    ): ReceiveChannel<Action<AsyncData<S>>> = actions {
        send { AsyncData.Loading }
        try {
            val result = load()
            send { AsyncData.Success(result) }
        } catch (e: Exception) {
            send { AsyncData.Error(e) }
        }
    }

    fun cleanUp() = Unit
}