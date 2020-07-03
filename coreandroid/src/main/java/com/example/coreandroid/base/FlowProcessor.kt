package com.example.coreandroid.base

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@ExperimentalCoroutinesApi
interface FlowProcessor<Intent : Any, Update : StateUpdate<State>, State : Any, Signal : Any> {
    fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<Intent>,
        currentState: () -> State,
        states: Flow<State>,
        intent: suspend (Intent) -> Unit,
        signal: suspend (Signal) -> Unit
    ): Flow<Update>

    fun stateWillUpdate(
        currentState: State,
        nextState: State,
        update: Update,
        savedStateHandle: SavedStateHandle
    ) = Unit
}
