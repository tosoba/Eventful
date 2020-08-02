package com.eventful.alarms

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.eventful.alarms.databinding.AddEditAlarmDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.parcel.Parcelize
import java.util.*

fun Fragment.showAddEditAlarmDialog(
    mode: AlarmMode,
    saveClicked: (Long) -> Unit
) {
    AddEditAlarmDialog(requireContext(), mode).apply {
        setContentView(
            AddEditAlarmDialogBinding.inflate(layoutInflater).apply {
                alarmDialogTitle.text = if (mode is AlarmMode.Edit) {
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
}

@Parcelize
private data class AlarmTime(val hourOfDay: Int, val minute: Int) : Parcelable {
    constructor(mode: AlarmMode) : this(mode.hour, mode.minute)
}

@Parcelize
private data class AlarmDate(val year: Int, val month: Int, val day: Int) : Parcelable {
    constructor(calendar: Calendar) : this(
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH]
    )
}

private class AddEditAlarmDialog(
    context: Context,
    private val mode: AlarmMode
) : BottomSheetDialog(context) {

    val time: MutableLiveData<AlarmTime> by lazy(LazyThreadSafetyMode.NONE) {
        MutableLiveData(AlarmTime(mode))
    }

    val date: MutableLiveData<AlarmDate> by lazy(LazyThreadSafetyMode.NONE) {
        MutableLiveData(AlarmDate(mode.startDateCalendar))
    }

    override fun onSaveInstanceState(): Bundle = super.onSaveInstanceState().apply {
        putParcelable(SAVED_TIME_KEY, time.value)
        putParcelable(SAVED_DATE_KEY, date.value)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getParcelable<AlarmTime>(SAVED_TIME_KEY)?.let { time.value = it }
        savedInstanceState.getParcelable<AlarmDate>(SAVED_DATE_KEY)?.let { date.value = it }
    }

    companion object {
        private const val SAVED_TIME_KEY = "SAVED_TIME_KEY"
        private const val SAVED_DATE_KEY = "SAVED_DATE_KEY"
    }
}

private val AlarmMode.hour: Int
    get() = when (this) {
        is AlarmMode.Add -> {
            val startTime = requireNotNull(event.startTime)
            val splitTime = startTime.split(':')
            splitTime.first().toInt()
        }
        is AlarmMode.Edit -> {
            val calendar = GregorianCalendar.getInstance()
            calendar.time = Date(alarm.timestamp)
            calendar[Calendar.HOUR_OF_DAY]
        }
    }

private val AlarmMode.minute: Int
    get() = when (this) {
        is AlarmMode.Add -> {
            val startTime = requireNotNull(event.startTime)
            val splitTime = startTime.split(':')
            splitTime.last().toInt()
        }
        is AlarmMode.Edit -> {
            val calendar = GregorianCalendar.getInstance()
            calendar.time = Date(alarm.timestamp)
            calendar[Calendar.MINUTE]
        }
    }

private val AlarmMode.startDateCalendar: Calendar
    get() = GregorianCalendar.getInstance().apply {
        when (this@startDateCalendar) {
            is AlarmMode.Add -> {
                time = requireNotNull(event.startDate)
                add(Calendar.DATE, -1)
            }
            is AlarmMode.Edit -> {
                time = Date(alarm.timestamp)
            }
        }
    }
