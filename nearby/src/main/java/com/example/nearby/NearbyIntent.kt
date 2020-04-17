package com.example.nearby

import com.example.coreandroid.ticketmaster.Event

sealed class NearbyIntent
object EventListScrolledToEnd : NearbyIntent()
data class EventLongClicked(val event: Event) : NearbyIntent()
object ClearSelectionClicked : NearbyIntent()
object AddToFavouritesClicked : NearbyIntent()