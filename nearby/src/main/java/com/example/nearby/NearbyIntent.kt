package com.example.nearby

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.AddToFavouritesIntent
import com.example.coreandroid.util.ClearEventSelectionIntent
import com.example.coreandroid.util.EventSelectionToggledIntent

sealed class NearbyIntent
object EventListScrolledToEnd : NearbyIntent()
data class EventLongClicked(override val event: Event) : NearbyIntent(), EventSelectionToggledIntent
object ClearSelectionClicked : NearbyIntent(), ClearEventSelectionIntent
object AddToFavouritesClicked : NearbyIntent(), AddToFavouritesIntent
