package com.eventful.core.usecase.alarm

import com.eventful.core.manager.IEventAlarmManager
import com.eventful.core.repo.IAlarmRepository
import javax.inject.Inject

class UpdateAlarm @Inject constructor(
    private val repo: IAlarmRepository,
    private val manager: IEventAlarmManager
) {
    suspend operator fun invoke(id: Int, timestamp: Long) {
        repo.updateAlarm(id, timestamp)
        manager.update(id, timestamp)
    }
}
