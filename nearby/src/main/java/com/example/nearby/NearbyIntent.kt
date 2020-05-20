package com.example.nearby

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.*

sealed class NearbyIntent
object EventListScrolledToEnd : NearbyIntent(), LoadMoreEventsIntent
data class EventLongClicked(override val event: Event) : NearbyIntent(), EventSelectionToggledIntent
object ClearSelectionClicked : NearbyIntent(), ClearEventSelectionIntent
object AddToFavouritesClicked : NearbyIntent(), AddToFavouritesIntent
object HideSnackbar : NearbyIntent(), HideSnackbarIntent
