package com.example.core.usecase.alarm

import com.example.core.model.alarm.IAlarm
import com.example.core.repo.IAlarmRepository
import javax.inject.Inject

class DeleteAlarms @Inject constructor(private val repo: IAlarmRepository) {
    suspend operator fun invoke(alarms: List<IAlarm>) {
        repo.deleteAlarms(alarms)
    }
}
