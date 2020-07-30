package com.eventful.core.usecase.alarm

import com.eventful.core.model.alarm.IAlarm
import com.eventful.core.repo.IAlarmRepository
import javax.inject.Inject

class InsertAlarm @Inject constructor(private val repo: IAlarmRepository) {
    suspend operator fun invoke(alarm: IAlarm) {
        repo.insertAlarm(alarm)
    }
}
