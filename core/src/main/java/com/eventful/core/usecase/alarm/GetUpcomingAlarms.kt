package com.eventful.core.usecase.alarm

import com.eventful.core.model.alarm.IAlarm
import com.eventful.core.repo.IAlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUpcomingAlarms @Inject constructor(private val repo: IAlarmRepository) {
    operator fun invoke(limit: Int): Flow<List<IAlarm>> = repo.getUpcomingAlarms(limit)
}
