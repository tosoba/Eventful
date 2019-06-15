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
    protected val viewStateStore: ViewStateStore<State>
) : ViewModel(), CoroutineScope {

    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val viewStateObservable: StateObservable<State>
        get() = viewStateStore

    val currentState: State
        get() = viewStateStore.currentState

    val liveState: LiveData<State>
        get() = viewStateStore.liveState

    override fun onCleared() {
        job.cancel()
    }
}