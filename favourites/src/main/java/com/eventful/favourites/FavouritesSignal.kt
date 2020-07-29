package com.eventful.favourites

sealed class FavouritesSignal {
    object FavouritesRemoved : FavouritesSignal()
}
