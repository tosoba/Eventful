package com.example.coreandroid.arch.state

interface Action<out T>

class StateTransition<State>(private val nextState: State.() -> State) : Action<State> {
    operator fun invoke(state: State) = state.nextState()
}

interface Signal : Action<Nothing>

data class ErrorSignal(val error: Throwable?, val message: String) : Signal {
    constructor(t: Throwable) : this(t, t.message ?: "Error ${t.javaClass.name}")
}