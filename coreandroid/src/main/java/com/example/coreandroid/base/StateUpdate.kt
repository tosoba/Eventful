package com.example.coreandroid.base

interface StateUpdate<State : Any> {
    operator fun invoke(state: State): State
}