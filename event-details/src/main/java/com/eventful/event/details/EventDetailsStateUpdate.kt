package com.eventful.event.details

import com.eventful.core.util.Data
import com.eventful.core.util.LoadedSuccessfully
import com.eventful.core.android.base.StateUpdate

sealed class EventDetailsStateUpdate : StateUpdate<EventDetailsState> {
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
