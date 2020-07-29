package com.eventful.event

sealed class EventSignal {
    data class FavouriteStateToggled(val isFavourite: Boolean) : EventSignal()
}
