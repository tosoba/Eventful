package com.example.favourites

import com.example.coreandroid.model.event.Event

sealed class FavouritesIntent {
    data class NewSearch(val text: String) : FavouritesIntent()
    object LoadFavourites : FavouritesIntent()
    object RemoveFromFavouritesClicked : FavouritesIntent()
    data class EventLongClicked(val event: Event) : FavouritesIntent()
    object ClearSelectionClicked : FavouritesIntent()
    object HideSnackbar : FavouritesIntent()
}
