package com.example.favourites

sealed class FavouritesSignal {
    object FavouritesRemoved : FavouritesSignal()
}