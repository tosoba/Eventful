package com.eventful.nearby

sealed class NearbySignal {
    object FavouritesSaved : NearbySignal()
    object EventsLoadingFinished : NearbySignal()
}
