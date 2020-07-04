package com.example.favourites

import com.example.core.util.DataList
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable

data class FavouriteEventsData(val searchText: String, val events: DataList<Selectable<Event>>)
