package com.example.coreandroid.arch.state

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

interface StateObservable<State : Any> {
    val liveState: LiveData<State>
    val currentState: State
    fun observe(owner: LifecycleOwner, observer: (State) -> Unit)
    fun observeSignals(owner: LifecycleOwner, executor: (Signal) -> Unit)
}

open class ViewDataStore<State : Any>(
    initialState: State
) : StateObservable<State> {
    private val mutableLiveState = MutableLiveData<State>().apply {
        value = initialState
    }
    override val liveState: LiveData<State> = mutableLiveState

    private val liveSignal = MutableLiveData<Signal>()

    override val currentState: State
        get() = mutableLiveState.value!!

    override fun observe(
        owner: LifecycleOwner, observer: (State) -> Unit
    ) = mutableLiveState.observe(owner, Observer { observer(it!!) })

    override fun observeSignals(
        owner: LifecycleOwner,
        executor: (Signal) -> Unit
    ) = liveSignal.observe(owner, Observer {
        executor(it)
    })

    @MainThread
    fun dispatchStateTransition(transition: StateTransition<State>) {
        mutableLiveState.value = transition(currentState)
    }

    @MainThread
    fun dispatchStateTransition(nextState: State.() -> State) {
        mutableLiveState.value = currentState.nextState()
    }

    @MainThread
    fun dispatchSignal(signal: Signal) {
        liveSignal.value = signal
    }
}

