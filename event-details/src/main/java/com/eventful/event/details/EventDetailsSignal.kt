package com.eventful.event.details

sealed class EventDetailsSignal {
    data class FavouriteStateToggled(val isFavourite: Boolean) : EventDetailsSignal()
}
