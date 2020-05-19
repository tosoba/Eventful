package com.example.search

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.*

sealed class SearchIntent
data class NewSearch(val text: String, val confirmed: Boolean) : SearchIntent()
object LoadMoreResults : SearchIntent(), LoadMoreEventsIntent
data class EventLongClicked(override val event: Event) : SearchIntent(), EventSelectionToggledIntent
object ClearSelectionClicked : SearchIntent(), ClearEventSelectionIntent
object AddToFavouritesClicked : SearchIntent(), AddToFavouritesIntent
object HideSnackbar : SearchIntent(), HideSnackbarIntent
