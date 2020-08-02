package com.eventful.alarms.dialog

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class AlarmDate(val year: Int, val month: Int, val day: Int) : Parcelable {
    constructor(calendar: Calendar) : this(
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH]
    )
}
