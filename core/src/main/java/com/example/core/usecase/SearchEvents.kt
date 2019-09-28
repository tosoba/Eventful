package com.example.core.usecase

import com.example.core.Resource
import com.example.core.model.PagedResult
import com.example.core.model.ticketmaster.IEvent
import com.example.core.repo.IEventsRepository
import javax.inject.Inject

class SearchEvents @Inject constructor(private val eventsRepository: IEventsRepository) {
    suspend operator fun invoke(
        searchText: String
    ): Resource<PagedResult<IEvent>> = eventsRepository.searchEvents(searchText)
}