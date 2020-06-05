package com.example.search

import com.example.coreandroid.ticketmaster.Event

sealed class SearchIntent {
    data class NewSearch(val text: String, val confirmed: Boolean) : SearchIntent()
    object LoadMoreResults : SearchIntent()
    data class EventLongClicked(val event: Event) : SearchIntent()
    object ClearSelectionClicked : SearchIntent()
    object AddToFavouritesClicked : SearchIntent()
    object HideSnackbar : SearchIntent()
}
