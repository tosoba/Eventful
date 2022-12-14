package com.eventful.core.usecase.event

import com.eventful.core.repo.IEventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsEventSavedFlow @Inject constructor(private val eventRepo: IEventRepository) {
    operator fun invoke(id: String): Flow<Boolean> = eventRepo.isEventSavedFlow(id)
}
