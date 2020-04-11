package com.example.coreandroid.arch

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseViewModel<Intent, State, Event>(initialState: State) : ViewModel() {

    protected val _states: ConflatedBroadcastChannel<State> = ConflatedBroadcastChannel(
        value = initialState
    )
    val states: Flow<State> get() = _states.asFlow().distinctUntilChanged()

    protected val _events = LiveEvent<Event>()
    val events: LiveData<Event> get() = _events

    protected val intents = BroadcastChannel<Intent>(capacity = Channel.CONFLATED)
    suspend fun send(intent: Intent) = intents.send(intent)
}

