package com.example.core.usecase

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.core.repo.IEventRepository
import javax.inject.Inject

class SearchEvents @Inject constructor(private val eventRepository: IEventRepository) {
    suspend operator fun invoke(
        searchText: String
    ): Resource<PagedResult<IEvent>> = eventRepository.searchEvents(searchText)
}