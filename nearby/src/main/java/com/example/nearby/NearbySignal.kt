package com.example.nearby

sealed class NearbySignal {
    object FavouritesSaved : NearbySignal()
    object EventsLoadingFinished : NearbySignal()
}
