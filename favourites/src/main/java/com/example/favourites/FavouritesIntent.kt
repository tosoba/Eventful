package com.example.favourites

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.ClearEventSelectionIntent
import com.example.coreandroid.util.EventSelectionToggledIntent

sealed class FavouritesIntent
object LoadFavourites : FavouritesIntent()
object RemoveFromFavouritesClicked : FavouritesIntent()
data class EventLongClicked(
    override val event: Event
) : FavouritesIntent(), EventSelectionToggledIntent

object ClearSelectionClicked : FavouritesIntent(), ClearEventSelectionIntent
