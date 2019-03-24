package com.example.coreandroid.arch.state

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

open class ViewDataStore<State : Any>(
    initialState: State
) {
    private val liveState = MutableLiveData<State>().apply {
        value = initialState
    }

    private val liveSignal = MutableLiveData<Signal>()

    val currentState: State
        get() = liveState.value!!

    fun observe(
        owner: LifecycleOwner,
        observer: (State) -> Unit
    ) = liveState.observe(owner, Observer { observer(it!!) })

    fun observeSignals(
        owner: LifecycleOwner,
        executor: (Signal) -> Unit
    ) = liveSignal.observe(owner, Observer {
        executor(it)
    })

    @MainThread
    fun dispatchStateTransition(transition: StateTransition<State>) {
        liveState.value = transition(currentState)
    }

    @MainThread
    fun dispatchSignal(signal: Signal) {
        liveSignal.value = signal
    }
}

