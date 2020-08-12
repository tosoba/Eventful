package com.eventful.alarms

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowProcessor
import com.eventful.core.android.base.removedAlarmsMsgRes
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.provider.CurrentEventProvider
import com.eventful.core.usecase.alarm.CreateAlarm
import com.eventful.core.usecase.alarm.DeleteAlarms
import com.eventful.core.usecase.alarm.GetAlarms
import com.eventful.core.usecase.alarm.UpdateAlarm
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
class AlarmsFlowProcessor(
    private val getAlarms: GetAlarms,
    private val deleteAlarms: DeleteAlarms,
    private val createAlarm: CreateAlarm,
    private val updateAlarm: UpdateAlarm,
    private val currentEventProvider: CurrentEventProvider?,
    private val ioDispatcher: CoroutineDispatcher
) : FlowProcessor<AlarmsIntent, AlarmsStateUpdate, AlarmsState, AlarmsSignal> {

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<AlarmsIntent>,
        currentState: () -> AlarmsState,
        states: Flow<AlarmsState>,
        intent: suspend (AlarmsIntent) -> Unit,
        signal: suspend (AlarmsSignal) -> Unit
    ): Flow<AlarmsStateUpdate> = intents
        .updates(coroutineScope, currentState, states, intent, signal)

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
        states: Flow<AlarmsState>,
        intent: suspend (AlarmsIntent) -> Unit,
        signal: suspend (AlarmsSignal) -> Unit
    ): Flow<AlarmsStateUpdate> = merge(
        currentEventProvider?.event?.map { AlarmsStateUpdate.NewEvent(it) } ?: emptyFlow(),
        states.map { it.mode }
            .distinctUntilChanged()
            .flatMapLatest(::loadAlarmsUpdates),
        filterIsInstance<AlarmsIntent.LoadMoreAlarms>()
            .loadMoreAlarmsUpdates(currentState),
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
        filterIsInstance<AlarmsIntent.UpdateAlarm>()
            .updateAlarmUpdates(signal),
        filterIsInstance<AlarmsIntent.DeleteAlarm>()
            .map { removeAlarms(listOf(it.id), coroutineScope, intent, signal) },
        filterIsInstance<AlarmsIntent.UpdateDialogStatus>()
            .map { (status) -> AlarmsStateUpdate.DialogStatus(status) }
    )

    private fun Flow<AlarmsIntent.LoadMoreAlarms>.loadMoreAlarmsUpdates(
        currentState: () -> AlarmsState
    ): Flow<AlarmsStateUpdate> = filterNot { currentState().items.limitHit }
        .flatMapLatest { loadAlarmsUpdates(currentState().mode) }

    private suspend fun loadAlarmsUpdates(mode: AlarmsMode): Flow<AlarmsStateUpdate> {
        return getAlarms(if (mode is AlarmsMode.SingleEvent) mode.event.id else null)
            .flowOn(ioDispatcher)
            .map { alarms -> AlarmsStateUpdate.Alarms(alarms = alarms) }
    }

    private fun Flow<AlarmsIntent.RemoveAlarmsClicked>.removeFromAlarmsUpdates(
        coroutineScope: CoroutineScope,
        currentState: () -> AlarmsState,
        intent: suspend (AlarmsIntent) -> Unit,
        signal: suspend (AlarmsSignal) -> Unit
    ): Flow<AlarmsStateUpdate> = map {
        removeAlarms(
            currentState().items.data.filter { it.selected }.map { it.item.id },
            coroutineScope,
            intent,
            signal
        )
    }

    private suspend fun removeAlarms(
        alarmIds: List<Int>,
        coroutineScope: CoroutineScope,
        intent: suspend (AlarmsIntent) -> Unit,
        signal: suspend (AlarmsSignal) -> Unit
    ): AlarmsStateUpdate {
        withContext(ioDispatcher) { deleteAlarms(alarmIds) }
        signal(AlarmsSignal.AlarmsRemoved)
        return AlarmsStateUpdate.RemovedAlarms(
            msgRes = SnackbarState.Shown.MsgRes(
                removedAlarmsMsgRes(alarmsCount = alarmIds.size),
                arrayOf(alarmIds.size)
            ),
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

    private fun Flow<AlarmsIntent.UpdateAlarm>.updateAlarmUpdates(
        signal: suspend (AlarmsSignal) -> Unit
    ): Flow<AlarmsStateUpdate> = map { (id, timestamp) ->
        updateAlarm(id, timestamp)
        signal(AlarmsSignal.AlarmUpdated)
        null
    }.filterNotNull()
}
