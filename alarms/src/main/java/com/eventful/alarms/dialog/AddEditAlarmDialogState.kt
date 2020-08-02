package com.eventful.alarms.dialog

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddEditAlarmDialogState(val time: AlarmTime, val date: AlarmDate) : Parcelable
