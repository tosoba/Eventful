package com.example.event

import com.example.core.util.Data
import com.example.core.util.LoadedSuccessfully
import com.example.coreandroid.util.StateUpdate

sealed class EventStateUpdate :
    StateUpdate<EventState> {
    sealed class FavouriteStatus : EventStateUpdate() {
        object Loading : FavouriteStatus() {
            override fun invoke(state: EventState): EventState = state.copy(
                isFavourite = state.isFavourite.copyWithLoadingStatus
            )
        }

        class Loaded(private val favourite: Boolean) : FavouriteStatus() {
            override fun invoke(state: EventState): EventState = state.copy(
                isFavourite = Data(
                    favourite,
                    LoadedSuccessfully
                )
            )
        }
    }
}
