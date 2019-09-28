package com.example.core.usecase

import com.example.core.model.search.SearchSuggestion
import com.example.core.repo.IEventsRepository
import javax.inject.Inject

class GetSeachSuggestions @Inject constructor(private val eventsRepo: IEventsRepository) {
    suspend operator fun invoke(
        searchText: String
    ): List<SearchSuggestion> = eventsRepo.getSearchSuggestions(searchText)
}