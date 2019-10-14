package com.example.nearby

import com.example.coreandroid.ticketmaster.Event

sealed class NearbyViewEvent

sealed class Interaction : NearbyViewEvent() {
    object EventListScrolledToEnd : Interaction()
    data class EventClicked(val event: Event) : Interaction()
    data class EventLongClicked(val event: Event) : Interaction()
    object ClearSelectionClicked : Interaction()
    data class AddToFavouritesClicked(val events: List<Event>) : Interaction()
}

sealed class Lifecycle : NearbyViewEvent() {
    data class OnViewCreated(val wasRecreated: Boolean) : Lifecycle()
    object OnDestroy : Lifecycle()
}
