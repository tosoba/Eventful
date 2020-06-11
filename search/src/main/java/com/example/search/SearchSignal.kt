package com.example.search

sealed class SearchSignal {
    object FavouritesSaved : SearchSignal()
}
