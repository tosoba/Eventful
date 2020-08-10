package com.eventful.core.usecase.alarm

import com.eventful.core.manager.IEventAlarmManager
import com.eventful.core.repo.IAlarmRepository
import javax.inject.Inject

class DeleteAlarms @Inject constructor(
    private val repo: IAlarmRepository,
    private val manager: IEventAlarmManager
) {
    suspend operator fun invoke(alarmIds: List<Int>) {
        if (alarmIds.isEmpty()) throw IllegalArgumentException("List of alarms ids to delete is empty.")
        repo.deleteAlarms(alarmIds)
        alarmIds.forEach(manager::cancel)
    }
}
