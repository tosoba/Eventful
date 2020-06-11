package com.example.core.usecase

import com.example.core.model.PagedResult
import com.example.core.model.Resource
import com.example.core.model.event.IEvent
import com.example.core.repo.IEventRepository
import javax.inject.Inject

class SearchEvents @Inject constructor(private val eventRepository: IEventRepository) {
    suspend operator fun invoke(
        searchText: String,
        offset: Int? = null
    ): Resource<PagedResult<IEvent>> = eventRepository.searchEvents(searchText, offset)
}
