package com.example.coreandroid.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coreandroid.arch.state.StateObservable
import com.example.coreandroid.arch.state.ViewStateStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class CoroutineViewModel<State : Any>(
    protected val stateStore: ViewStateStore<State>
) : ViewModel(), CoroutineScope {

    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val viewStateObservable: StateObservable<State>
        get() = stateStore

    val currentState: State
        get() = stateStore.currentState

    val liveState: LiveData<State>
        get() = stateStore.liveState

    override fun onCleared() {
        job.cancel()
    }
}