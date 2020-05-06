package com.example.coreandroid.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coreandroid.util.withLatestFrom
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
abstract class BaseViewModel<Intent, State : Any, Event>(initialState: State) : ViewModel() {

    protected val statesChannel: ConflatedBroadcastChannel<State> = ConflatedBroadcastChannel(
        value = initialState
    )
    val states: Flow<State> get() = statesChannel.asFlow().distinctUntilChanged()
    val state: State get() = statesChannel.value

    protected val liveEvents = LiveEvent<Event>()
    val events: LiveData<Event> get() = liveEvents

    protected val intentsChannel = BroadcastChannel<Intent>(capacity = Channel.CONFLATED)
    suspend fun send(intent: Intent) = intentsChannel.send(intent)

    protected val intentsWithLatestStates: Flow<Pair<Intent, State>>
        get() = intentsChannel.asFlow()
            .withLatestFrom(states) { intent, state -> intent to state }

    override fun onCleared() {
        intentsChannel.close()
        statesChannel.close()
        super.onCleared()
    }
}

