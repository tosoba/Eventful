package com.example.nearby

import com.example.coreandroid.model.event.Event

sealed class NearbyIntent {
    object LoadMoreResults : NearbyIntent()
    data class EventLongClicked(val event: Event) : NearbyIntent()
    object ClearSelectionClicked : NearbyIntent()
    object AddToFavouritesClicked : NearbyIntent()
    object HideSnackbar : NearbyIntent()
}
