package com.eventful.event

import com.eventful.core.util.Data
import com.eventful.core.util.LoadedSuccessfully
import com.eventful.core.android.base.StateUpdate

sealed class EventStateUpdate : StateUpdate<EventState> {
    sealed class FavouriteStatus : EventStateUpdate() {
        object Loading : FavouriteStatus() {
            override fun invoke(state: EventState): EventState = state.copy(
                isFavourite = state.isFavourite.copyWithLoadingStatus
            )
        }

        class Loaded(private val favourite: Boolean) : FavouriteStatus() {
            override fun invoke(state: EventState): EventState = state.copy(
                isFavourite = Data(favourite, LoadedSuccessfully)
            )
        }
    }
}
