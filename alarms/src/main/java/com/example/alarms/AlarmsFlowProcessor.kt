package com.example.alarms

import com.example.coreandroid.base.FlowProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalCoroutinesApi
class AlarmsFlowProcessor @Inject constructor() : FlowProcessor<AlarmsIntent, AlarmsStateUpdate, AlarmsState, AlarmsSignal> {
    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<AlarmsIntent>,
        currentState: () -> AlarmsState,
        states: Flow<AlarmsState>,
        intent: suspend (AlarmsIntent) -> Unit,
        signal: suspend (AlarmsSignal) -> Unit
    ): Flow<AlarmsStateUpdate> {
        TODO("Not yet implemented")
    }
}
