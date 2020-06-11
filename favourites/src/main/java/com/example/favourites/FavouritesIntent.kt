package com.example.favourites

import com.example.coreandroid.model.Event

sealed class FavouritesIntent {
    object LoadFavourites : FavouritesIntent()
    object RemoveFromFavouritesClicked : FavouritesIntent()
    data class EventLongClicked(val event: Event) : FavouritesIntent()
    object ClearSelectionClicked : FavouritesIntent()
    object HideSnackbar : FavouritesIntent()
}
