package com.eventful.core.usecase

import com.eventful.core.model.alarm.IAlarm
import com.eventful.core.repo.IAlarmRepository
import javax.inject.Inject

class DeleteAlarms @Inject constructor(private val repo: IAlarmRepository) {
    suspend operator fun invoke(alarms: List<IAlarm>) {
        repo.deleteAlarms(alarms)
    }
}
