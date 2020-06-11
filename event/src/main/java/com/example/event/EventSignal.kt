package com.example.event

sealed class EventSignal {
    data class FavouriteStateToggled(val isFavourite: Boolean) : EventSignal()
}
