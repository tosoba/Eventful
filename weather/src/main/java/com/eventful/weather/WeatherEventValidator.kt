package com.eventful.weather

import com.eventful.core.android.model.event.Event

object WeatherEventValidator {
    fun isValid(event: Event): Boolean = if (!event.startDateTimeSetInFuture) {
        false
    } else {
        event.venues?.firstOrNull()?.run { latLng != null && city != null } ?: false
    }
}
