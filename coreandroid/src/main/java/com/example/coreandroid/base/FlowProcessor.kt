package com.example.coreandroid.base

import androidx.lifecycle.SavedStateHandle
import com.example.coreandroid.util.StateUpdate
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
        states: StateFlow<State>,
        intent: suspend (Intent) -> Unit,
        signal: suspend (Signal) -> Unit,
        savedStateHandle: SavedStateHandle
    ): Flow<Update>
}
