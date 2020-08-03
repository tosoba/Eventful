package com.eventful.alarms

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowProcessor
import com.eventful.core.android.base.removedFromAlarmsMessage
import com.eventful.core.usecase.alarm.DeleteAlarms
import com.eventful.core.usecase.alarm.GetAlarms
import com.eventful.core.usecase.alarm.CreateAlarm
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class AlarmsFlowProcessor(
    private val getAlarms: GetAlarms,
    private val deleteAlarms: DeleteAlarms,
    private val createAlarm: CreateAlarm,
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

    override fun stateWillUpdate(
        currentState: AlarmsState,
        nextState: AlarmsState,
        update: AlarmsStateUpdate,
        savedStateHandle: SavedStateHandle
    ) {
        if (update is AlarmsStateUpdate.DialogStatus) {
            savedStateHandle[AlarmsState.KEY_DIALOG_STATUS] = update.status
        }
    }

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
            .removeFromAlarmsUpdates(coroutineScope, currentState, intent, signal),
        filterIsInstance<AlarmsIntent.AddAlarm>()
            .addAlarmUpdates(signal),
        filterIsInstance<AlarmsIntent.UpdateDialogStatus>()
            .map { (status) -> AlarmsStateUpdate.DialogStatus(status) }
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

    private fun Flow<AlarmsIntent.AddAlarm>.addAlarmUpdates(
        signal: suspend (AlarmsSignal) -> Unit
    ): Flow<AlarmsStateUpdate> = map { (event, timestamp) ->
        createAlarm(event.id, timestamp)
        signal(AlarmsSignal.AlarmAdded)
        null
    }.filterNotNull()
}
