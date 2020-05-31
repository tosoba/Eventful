package com.example.coreandroid.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.SelectableEventsSnackbarState
import com.example.coreandroid.util.SelectableEventsState
import com.example.coreandroid.util.withLatestFrom
import com.google.android.material.snackbar.Snackbar
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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
abstract class BaseStateFlowViewModel<Intent : Any, State : Any, Signal : Any>(
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
    protected var state: State
        set(value) = value.let { _states.value = it }
        get() = _states.value

    override fun onCleared() {
        _intents.close()
        _signals.close()
        super.onCleared()
    }

    protected fun <Update : StateUpdate<State>> Flow<Update>.applyToState(initialState: State): Job {
        return scan(initialState) { state, update -> update(state) }
            .onEach { state = it }
            .launchIn(viewModelScope)
    }
}

interface StateUpdate<State : Any> {
    operator fun invoke(state: State): State
}

interface ClearSelectionUpdate<S : SelectableEventsState<S>> : StateUpdate<S> {
    override fun invoke(state: S): S = state.copyWithTransformedEvents { it.copy(selected = false) }
}

interface ToggleEventSelectionUpdate<S : SelectableEventsState<S>> : StateUpdate<S> {
    val event: Event
    override fun invoke(state: S): S = state.copyWithTransformedEvents {
        if (it.item.id == event.id) Selectable(event, !it.selected) else it
    }
}

interface AddedToFavouritesUpdate<S : SelectableEventsSnackbarState<S>> : StateUpdate<S> {
    val addedCount: Int
    val onDismissed: () -> Unit
    override fun invoke(state: S): S = state.copyWithSnackbarStateAndTransformedEvents(
        snackbarState = SnackbarState.Shown(
            """$addedCount
                |${if (addedCount > 1) " events were" else " event was"} 
                |added to favourites""".trimMargin().replace("\n", ""),
            length = Snackbar.LENGTH_SHORT,
            onDismissed = onDismissed
        )
    ) { event -> event.copy(selected = false) }
}
