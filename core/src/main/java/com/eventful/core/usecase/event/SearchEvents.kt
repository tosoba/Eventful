package com.eventful.core.usecase.event

import com.eventful.core.model.PagedResult
import com.eventful.core.model.Resource
import com.eventful.core.model.event.IEvent
import com.eventful.core.repo.IEventRepository
import javax.inject.Inject

class SearchEvents @Inject constructor(private val eventRepository: IEventRepository) {
    suspend operator fun invoke(
        searchText: String,
        offset: Int? = null
    ): Resource<PagedResult<IEvent>> = eventRepository.searchEvents(searchText, offset)
}
