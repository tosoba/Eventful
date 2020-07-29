package com.eventful.search

sealed class SearchSignal {
    object FavouritesSaved : SearchSignal()
}
