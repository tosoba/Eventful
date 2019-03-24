package com.example.coreandroid.arch.state.ext

import com.example.coreandroid.arch.state.Action
import com.example.coreandroid.arch.state.Signal
import com.example.coreandroid.arch.state.StateTransition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.fold

@ObsoleteCoroutinesApi
suspend inline fun <T : Any> ReceiveChannel<Action<T>>.dispatchAndCollect(
    initialState: T
): List<Any> = fold(emptyList()) { states, action ->
    val element: Any = if (action is StateTransition)
        action(states.lastOrNull() as T? ?: initialState)
    else action
    states + element
}

@ObsoleteCoroutinesApi
suspend inline fun <reified State : Any> CoroutineScope.dispatchAndCollectStates(
    initialState: State,
    crossinline makeActions: suspend CoroutineScope.(State) -> ReceiveChannel<Action<State>>
): List<State> = makeActions(initialState)
    .dispatchAndCollect(initialState)
    .filterIsInstance<State>()

@ObsoleteCoroutinesApi
suspend inline fun <reified State : Any> CoroutineScope.dispatchAndCollectSignals(
    initialState: State,
    crossinline makeActions: suspend CoroutineScope.(State) -> ReceiveChannel<Action<State>>
): List<Signal> = makeActions(initialState)
    .dispatchAndCollect(initialState)
    .filterIsInstance<Signal>()