package com.example.coreandroid.mapper

import com.example.core.model.event.Event
import com.example.coreandroid.model.EventUiModel

val Event.ui: EventUiModel
    get() = EventUiModel(
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