package com.eventful.alarms.dialog

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AlarmTime(val hourOfDay: Int, val minute: Int) : Parcelable {
    constructor(mode: AddEditAlarmDialogMode) : this(mode.hour, mode.minute)
}
