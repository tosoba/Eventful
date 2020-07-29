package com.eventful.core.android.base

interface StateUpdate<State : Any> {
    operator fun invoke(state: State): State
}