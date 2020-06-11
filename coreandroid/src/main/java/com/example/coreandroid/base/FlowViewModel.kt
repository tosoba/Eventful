package com.example.coreandroid.base

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreandroid.util.StateUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
abstract class FlowViewModel<Intent : Any, Update : StateUpdate<State>, State : Any, Signal : Any>(
    initialState: State,
    processor: FlowProcessor<Intent, Update, State, Signal>,
    savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private val _signals: BroadcastChannel<Signal> = BroadcastChannel(Channel.BUFFERED)
    val signals: Flow<Signal> get() = _signals.asFlow()

    private val _intents: BroadcastChannel<Intent> = BroadcastChannel(Channel.CONFLATED)
    suspend fun intent(intent: Intent) = _intents.send(intent)

    private val _states: MutableStateFlow<State> = MutableStateFlow(initialState)
    val states: StateFlow<State> get() = _states
    var state: State
        private set(value) = value.let { _states.value = it }
        get() = _states.value

    init {
        processor
            .updates(
                coroutineScope = viewModelScope,
                intents = _intents.asFlow(),
                currentState = states::value,
                intent = ::intent,
                signal = _signals::send,
                savedStateHandle = savedStateHandle
            )
            .onEach { Log.e("UPDATE", it.toString()) }
            .scan(initialState) { state, update -> update(state) }
            .onEach {
                Log.e("STATE", it.toString())
                state = it
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        _intents.close()
        _signals.close()
        super.onCleared()
    }
}
