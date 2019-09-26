package com.example.search

import com.example.coreandroid.ticketmaster.Event

sealed class SearchViewEvent

sealed class Interaction : SearchViewEvent() {
    data class SearchTextChanged(val searchText: String) : Interaction()
    object EventListScrolledToEnd : Interaction()
    data class EventClicked(val event: Event) : Interaction()
}

sealed class Lifecycle : SearchViewEvent() {
    object OnDestroy : Lifecycle()
}
