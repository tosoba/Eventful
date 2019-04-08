package com.example.api.mapper

import com.example.api.model.EventApiModel
import com.example.core.model.Event

val EventApiModel.core: Event
    get() = Event(
        category,
        country,
        description,
        duration,
        end,
        firstSeen,
        id,
        labels,
        localRank,
        location,
        rank,
        relevance,
        scope,
        start,
        state,
        timezone,
        title,
        updated
    )