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
    protected suspend fun <S> ProducerScope<Action<S>>.transition(nextState: S.() -> S) {
        send(StateTransition(nextState))
    }

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    protected suspend fun <T> ProducerScope<Action<T>>.actions(channel: ReceiveChannel<Action<T>>) {
        channel.consumeEach { originalAction -> send(originalAction) }
    }

    @ExperimentalCoroutinesApi
    protected fun <T> CoroutineScope.produceActions(
        actionsProducer: suspend ProducerScope<Action<T>>.() -> Unit
    ): ReceiveChannel<Action<T>> = produce(block = actionsProducer)

    @ExperimentalCoroutinesApi
    protected inline fun <S> CoroutineScope.loadAsyncDataActions(
        crossinline load: suspend () -> S
    ): ReceiveChannel<Action<AsyncData<S>>> = produceActions {
        transition { AsyncData.Loading }
        try {
            val result = load()
            transition { AsyncData.Success(result) }
        } catch (e: Exception) {
            transition { AsyncData.Error(e) }
        }
    }

    fun cleanUp() = Unit
}