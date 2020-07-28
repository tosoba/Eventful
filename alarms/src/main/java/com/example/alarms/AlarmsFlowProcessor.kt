package com.example.alarms

import com.example.core.usecase.alarm.DeleteAlarms
import com.example.core.usecase.alarm.GetAlarms
import com.example.coreandroid.base.FlowProcessor
import com.example.coreandroid.base.removedFromAlarmsMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
class AlarmsFlowProcessor(
    private val getAlarms: GetAlarms,
    private val deleteAlarms: DeleteAlarms,
    private val ioDispatcher: CoroutineDispatcher,
    private val loadAlarmsOnStart: Boolean = true
) : FlowProcessor<AlarmsIntent, AlarmsStateUpdate, AlarmsState, AlarmsSignal> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<AlarmsIntent>,
        currentState: () -> AlarmsState,
        states: Flow<AlarmsState>,
        intent: suspend (AlarmsIntent) -> Unit,
        signal: suspend (AlarmsSignal) -> Unit
    ): Flow<AlarmsStateUpdate> = intents
        .run {
            if (loadAlarmsOnStart) onStart { emit(AlarmsIntent.LoadAlarms) }
            else this
        }
        .updates(coroutineScope, currentState, intent, signal)

    private fun Flow<AlarmsIntent>.updates(
        coroutineScope: CoroutineScope,
        currentState: () -> AlarmsState,
        intent: suspend (AlarmsIntent) -> Unit,
        signal: suspend (AlarmsSignal) -> Unit
    ): Flow<AlarmsStateUpdate> = merge(
        filterIsInstance<AlarmsIntent.LoadAlarms>()
            .loadAlarmsUpdates(currentState),
        filterIsInstance<AlarmsIntent.AlarmLongClicked>()
            .map { (alarm) -> AlarmsStateUpdate.ToggleAlarmSelection(alarm) },
        filterIsInstance<AlarmsIntent.ClearSelectionClicked>()
            .map { AlarmsStateUpdate.ClearSelection },
        filterIsInstance<AlarmsIntent.HideSnackbar>()
            .map { AlarmsStateUpdate.HideSnackbar },
        filterIsInstance<AlarmsIntent.RemoveAlarmsClicked>()
            .removeFromAlarmsUpdates(coroutineScope, currentState, intent, signal)
    )

    private fun Flow<AlarmsIntent.LoadAlarms>.loadAlarmsUpdates(
        currentState: () -> AlarmsState
    ): Flow<AlarmsStateUpdate> = filterNot { currentState().items.limitHit }
        .flatMapLatest {
            val eventId = currentState().mode.let { mode ->
                if (mode is AlarmsMode.SingleEvent) mode.event.id else null
            }
            getAlarms(eventId)
                .flowOn(ioDispatcher)
                .map { alarms -> AlarmsStateUpdate.Alarms(alarms = alarms) }
        }

    private fun Flow<AlarmsIntent.RemoveAlarmsClicked>.removeFromAlarmsUpdates(
        coroutineScope: CoroutineScope,
        currentState: () -> AlarmsState,
        intent: suspend (AlarmsIntent) -> Unit,
        signal: suspend (AlarmsSignal) -> Unit
    ): Flow<AlarmsStateUpdate> = map {
        val selectedAlarms = currentState().items.data.filter { it.selected }.map { it.item }
        withContext(ioDispatcher) { deleteAlarms(selectedAlarms) }
        signal(AlarmsSignal.AlarmsRemoved)
        AlarmsStateUpdate.RemovedAlarms(
            snackbarText = removedFromAlarmsMessage(alarmsCount = selectedAlarms.size),
            onSnackbarDismissed = {
                coroutineScope.launch { intent(AlarmsIntent.HideSnackbar) }
            }
        )
    }
}
