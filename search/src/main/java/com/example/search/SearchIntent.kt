package com.example.search

import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.AddToFavouritesIntent
import com.example.coreandroid.util.ClearEventSelectionIntent
import com.example.coreandroid.util.EventSelectionToggledIntent
import com.example.coreandroid.util.HideSnackbarIntent

sealed class SearchIntent
data class NewSearch(val text: String, val confirmed: Boolean) : SearchIntent()
data class LoadMoreResults(val offset: Int? = null) : SearchIntent()
data class EventLongClicked(override val event: Event) : SearchIntent(), EventSelectionToggledIntent
object ClearSelectionClicked : SearchIntent(), ClearEventSelectionIntent
object AddToFavouritesClicked : SearchIntent(), AddToFavouritesIntent
object HideSnackbar : SearchIntent(), HideSnackbarIntent
