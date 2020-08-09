package com.eventful.event.details

import com.eventful.core.android.base.StateUpdate
import com.eventful.core.android.model.event.Event
import com.eventful.core.util.Data
import com.eventful.core.util.LoadedSuccessfully

sealed class EventDetailsStateUpdate : StateUpdate<EventDetailsState> {
    data class NewEvent(val event: Event) : EventDetailsStateUpdate() {
        override fun invoke(state: EventDetailsState): EventDetailsState = state.copy(
            event = event
        )
    }

    sealed class FavouriteStatus : EventDetailsStateUpdate() {
        object Loading : FavouriteStatus() {
            override fun invoke(state: EventDetailsState): EventDetailsState = state.copy(
                isFavourite = state.isFavourite.copyWithLoadingStatus
            )
        }

        class Loaded(private val favourite: Boolean) : FavouriteStatus() {
            override fun invoke(state: EventDetailsState): EventDetailsState = state.copy(
                isFavourite = Data(favourite, LoadedSuccessfully)
            )
        }
    }
}
