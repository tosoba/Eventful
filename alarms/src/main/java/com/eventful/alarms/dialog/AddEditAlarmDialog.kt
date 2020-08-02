package com.eventful.alarms.dialog

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.eventful.alarms.R
import com.eventful.alarms.databinding.AddEditAlarmDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*

fun Fragment.showAddEditAlarmDialog(
    mode: AddEditAlarmDialogMode,
    initialState: AddEditAlarmDialogState?,
    saveClicked: (Long) -> Unit
): AddEditAlarmDialog = AddEditAlarmDialog(requireContext(), mode, initialState).apply {
    setContentView(
        AddEditAlarmDialogBinding.inflate(layoutInflater).apply {
            alarmDialogTitle.text = if (mode is AddEditAlarmDialogMode.Edit) {
                context.getString(R.string.edit_alarm)
            } else {
                context.getString(R.string.add_alarm)
            }

            alarmSaveBtn.setOnClickListener {
                val alarmTime = requireNotNull(time.value)
                val alarmDate = requireNotNull(date.value)
                val timestamp = Calendar.getInstance().apply {
                    set(Calendar.YEAR, alarmDate.year)
                    set(Calendar.MONTH, alarmDate.month)
                    set(Calendar.DAY_OF_MONTH, alarmDate.day)
                    set(Calendar.HOUR_OF_DAY, alarmTime.hourOfDay)
                    set(Calendar.MINUTE, alarmTime.minute)
                }.timeInMillis
                saveClicked(timestamp)
                dismiss()
            }

            alarmCancelBtn.setOnClickListener { dismiss() }

            time.observe({ lifecycle }) { (hour, minute) ->
                alarmTimeTxt.text =
                    "${String.format("%02d", hour)}:${String.format("%02d", minute)}"
            }

            editTimeBtn.setOnClickListener {
                TimePickerDialog(
                    context,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        time.value = AlarmTime(hourOfDay, minute)
                    },
                    mode.hour,
                    mode.minute,
                    true
                ).show()
            }

            date.observe({ lifecycle }) { (year, month, day) ->
                alarmDateTxt.text =
                    "${String.format("%02d", day)}.${String.format("%02d", month + 1)}.$year"
            }

            editDateBtn.setOnClickListener {
                val calendar = mode.startDateCalendar
                DatePickerDialog(
                    context,
                    DatePickerDialog.OnDateSetListener { _, year, month, day ->
                        date.value = AlarmDate(year, month, day)
                    },
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH],
                    calendar[Calendar.DAY_OF_MONTH]
                ).show()
            }
        }.root
    )
    show()
}

class AddEditAlarmDialog(
    context: Context,
    val mode: AddEditAlarmDialogMode,
    initialState: AddEditAlarmDialogState?
) : BottomSheetDialog(context) {
    val time: MutableLiveData<AlarmTime> = MutableLiveData(
        initialState?.time ?: AlarmTime(mode)
    )
    val date: MutableLiveData<AlarmDate> = MutableLiveData(
        initialState?.date ?: AlarmDate(mode.startDateCalendar)
    )
    val state: AddEditAlarmDialogState
        get() = AddEditAlarmDialogState(time.value!!, date.value!!)
}
