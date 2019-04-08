package com.example.coreandroid.arch.state

import android.os.AsyncTask
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlin.coroutines.CoroutineContext

class CoViewStateStore<State : Any>(
    initialState: State,
    defaultDispatcher: CoroutineDispatcher
) : ViewDataStore<State>(initialState), CoroutineScope {

    private val contextJob = Job()

    override val coroutineContext: CoroutineContext = contextJob + defaultDispatcher

    fun coDispatch(makeAction: suspend () -> Action<State>): Job = launch {
        val action = makeAction()
        withContext(Dispatchers.Main) {
            dispatch(action)
        }
    }

    @ObsoleteCoroutinesApi
    fun coDispatch(
        makeActions: suspend CoroutineScope.(State) -> ReceiveChannel<Action<State>>
    ): Job = launch {
        makeActions(currentState).consumeEach { action ->
            withContext(Dispatchers.Main) {
                dispatch(action)
            }
        }
    }

    fun dispose() {
        contextJob.cancel()
    }

    private fun dispatch(action: Action<State>) {
        if (action is StateTransition<State>) {
            dispatchStateTransition(action)
        } else if (action is Signal) {
            dispatchSignal(action)
        }
    }

    companion object {
        val TEST_DISPATCHER = AsyncTask.THREAD_POOL_EXECUTOR.asCoroutineDispatcher()

        fun <T : Any> TEST(initialState: T): CoViewStateStore<T> = CoViewStateStore(initialState, TEST_DISPATCHER)
    }
}