package com.eventful.alarms.dialog

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class AddEditAlarmDialogStatus : Parcelable {
    @Parcelize object Hidden : AddEditAlarmDialogStatus()

    sealed class WithMode : AddEditAlarmDialogStatus() {
        abstract val mode: AddEditAlarmDialogMode

        @Parcelize
        class ShownWithState(
            override val mode: AddEditAlarmDialogMode,
            val state: AddEditAlarmDialogState
        ) : WithMode()

        @Parcelize data class Shown(override val mode: AddEditAlarmDialogMode) : WithMode()
    }
}
