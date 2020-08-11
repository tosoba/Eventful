package com.eventful.weather

import androidx.lifecycle.SavedStateHandle
import com.eventful.core.android.base.FlowProcessor
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.provider.CurrentEventProvider
import com.eventful.core.usecase.weather.GetForecast
import com.eventful.core.util.Initial
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class WeatherFlowProcessor @Inject constructor(
    private val getForecast: GetForecast,
    private val currentEventProvider: CurrentEventProvider,
    private val ioDispatcher: CoroutineDispatcher
) : FlowProcessor<WeatherIntent, WeatherStateUpdate, WeatherState, Unit> {

    override fun stateWillUpdate(
        currentState: WeatherState,
        nextState: WeatherState,
        update: WeatherStateUpdate,
        savedStateHandle: SavedStateHandle
    ) {
        if (update is WeatherStateUpdate.NewEvent) {
            savedStateHandle[WeatherArgs.EVENT.name] = update.event
        }
    }

    override fun updates(
        coroutineScope: CoroutineScope,
        intents: Flow<WeatherIntent>,
        currentState: () -> WeatherState,
        states: Flow<WeatherState>,
        intent: suspend (WeatherIntent) -> Unit,
        signal: suspend (Unit) -> Unit
    ): Flow<WeatherStateUpdate> = merge(
        currentEventProvider.event
            .filter { WeatherEventValidator.isValid(it) }
            .map { WeatherStateUpdate.NewEvent(it) },
        intents.filterIsInstance<WeatherIntent.TabSelected>()
            .map { WeatherStateUpdate.TabSelected(it.tab) },
        states.map { it.tab }
            .distinctUntilChanged()
            .filter { tab ->
                currentState()
                    .run {
                        when (tab) {
                            WeatherTab.NOW -> forecastNow
                            WeatherTab.EVENT_TIME -> forecastEventTime
                        }
                    }
                    .status is Initial
            }
            .flatMapLatest { tab ->
                currentState().event.let {
                    weatherLoadingUpdates(
                        it.venueLatLng,
                        timestampMillis(tab, it),
                        false,
                        coroutineScope,
                        intent
                    )
                }
            },
        states.map { (event) -> event.venueLatLng }
            .distinctUntilChanged()
            .flatMapLatest { latLng ->
                weatherLoadingUpdates(
                    latLng,
                    currentState().run { timestampMillis(tab, event) },
                    true,
                    coroutineScope,
                    intent
                )
            },
        intents.filterIsInstance<WeatherIntent.RetryLoadWeather>()
            .map { currentState().run { event.venueLatLng to timestampMillis(tab, event) } }
            .flatMapLatest { (latLng, timestampMillis) ->
                weatherLoadingUpdates(latLng, timestampMillis, false, coroutineScope, intent)
            }
    )

    private fun timestampMillis(tab: WeatherTab, event: Event): Long? = when (tab) {
        WeatherTab.NOW -> null
        WeatherTab.EVENT_TIME -> requireNotNull(event.startDate?.time)
    }

    private val Event.venueLatLng: LatLng get() = requireNotNull(venues?.firstOrNull()?.latLng)

    private suspend fun weatherLoadingUpdates(
        latLng: LatLng,
        timestampMillis: Long?,
        newEvent: Boolean,
        coroutineScope: CoroutineScope,
        intent: suspend (WeatherIntent) -> Unit
    ): Flow<WeatherStateUpdate> = flow<WeatherStateUpdate> {
        val tab = if (timestampMillis == null) WeatherTab.NOW else WeatherTab.EVENT_TIME
        emit(WeatherStateUpdate.Weather.Loading(tab))
        val resource = withContext(ioDispatcher) {
            getForecast(
                lat = latLng.latitude,
                lon = latLng.longitude,
                timestampMillis = timestampMillis
            )
        }
        emit(
            WeatherStateUpdate.Weather.Loaded(
                resource = resource,
                tab = tab,
                newEvent = newEvent,
                retry = { coroutineScope.launch { intent(WeatherIntent.RetryLoadWeather) } }
            )
        )
    }
}
