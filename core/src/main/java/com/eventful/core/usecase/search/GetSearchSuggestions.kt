package com.eventful.core.usecase.search

import com.eventful.core.model.search.SearchSuggestion
import com.eventful.core.repo.IEventRepository
import javax.inject.Inject

class GetSearchSuggestions @Inject constructor(private val eventRepo: IEventRepository) {
    suspend operator fun invoke(
        searchText: String
    ): List<SearchSuggestion> = eventRepo.getSearchSuggestions(searchText)
}
