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
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseViewModel<Intent, State : Any, Signal>(initialState: State) : ViewModel() {

    protected val statesChannel = ConflatedBroadcastChannel(value = initialState)
    val states: Flow<State> get() = statesChannel.asFlow().distinctUntilChanged()
    val state: State get() = statesChannel.value

    protected val liveSignals = LiveEvent<Signal>()
    val signals: LiveData<Signal> get() = liveSignals

    protected val intentsChannel = BroadcastChannel<Intent>(capacity = Channel.CONFLATED)
    suspend fun send(intent: Intent) = intentsChannel.send(intent)

    protected val intentsWithLatestStates: Flow<Pair<Intent, State>>
        get() = intentsChannel.asFlow().withLatestFrom(states) { intent, state -> intent to state }

    protected fun <T> Flow<T>.withLatestState(): Flow<Pair<T, State>> {
        return withLatestFrom(states) { item, state -> item to state }
    }

    override fun onCleared() {
        intentsChannel.close()
        statesChannel.close()
        super.onCleared()
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseFlowViewModel<Intent, State : Any, Signal>(initialState: State) : ViewModel() {
    private val _signals: BroadcastChannel<Signal> = BroadcastChannel(Channel.BUFFERED)
    val signals: Flow<Signal> get() = _signals.asFlow()
    protected suspend fun signal(signal: Signal) = _signals.send(signal)

    private val _intents: BroadcastChannel<Intent> = BroadcastChannel(Channel.CONFLATED)
    protected val intents: Flow<Intent> get() = _intents.asFlow()
    suspend fun intent(intent: Intent) = _intents.send(intent)

    private val _states: MutableStateFlow<State> = MutableStateFlow(initialState)
    val states: StateFlow<State> get() = _states
    protected var state: State
        set(value) = value.let { _states.value = it }
        get() = _states.value

    override fun onCleared() {
        _intents.close()
        _signals.close()
        super.onCleared()
    }
}
