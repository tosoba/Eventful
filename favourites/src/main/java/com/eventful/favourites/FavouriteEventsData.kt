package com.eventful.favourites

import com.eventful.core.util.DataList
import com.eventful.core.android.model.event.Event
import com.eventful.core.model.Selectable

data class FavouriteEventsData(val searchText: String, val events: DataList<Selectable<Event>>)
