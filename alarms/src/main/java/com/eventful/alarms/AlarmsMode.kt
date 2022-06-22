package com.eventful.alarms

import android.os.Parcelable
import com.eventful.core.android.model.event.Event
import kotlinx.android.parcel.Parcelize

sealed class AlarmsMode : Parcelable {
    @Parcelize object All : AlarmsMode()

    @Parcelize data class SingleEvent(val event: Event) : AlarmsMode()
}
