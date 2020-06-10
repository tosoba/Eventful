package com.example.coreandroid.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreandroid.util.StateUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseViewModel<Intent : Any, State : Any, Signal : Any>(
    initialState: State
) : ViewModel() {
    private val _signals: BroadcastChannel<Signal> = BroadcastChannel(Channel.BUFFERED)
    val signals: Flow<Signal> get() = _signals.asFlow()
    protected suspend fun signal(signal: Signal) = _signals.send(signal)

    private val _intents: BroadcastChannel<Intent> = BroadcastChannel(Channel.CONFLATED)
    protected val intents: Flow<Intent> get() = _intents.asFlow()
    suspend fun intent(intent: Intent) = _intents.send(intent)

    private val _states: MutableStateFlow<State> = MutableStateFlow(initialState)
    val states: StateFlow<State> get() = _states
    var state: State
        protected set(value) = value.let { _states.value = it }
        get() = _states.value

    override fun onCleared() {
        _intents.close()
        _signals.close()
        super.onCleared()
    }

    protected fun <Update : StateUpdate<State>> Flow<Update>.applyToState(
        initialState: State
    ): Job = onEach { Log.e("UPDATE", it.toString()) }
        .scan(initialState) { state, update -> update(state) }
        .onEach {
            Log.e("STATE", it.toString())
            state = it
        }
        .launchIn(viewModelScope)
}
