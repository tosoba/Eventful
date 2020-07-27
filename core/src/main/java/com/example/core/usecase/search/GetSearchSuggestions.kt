package com.example.core.usecase.search

import com.example.core.model.search.SearchSuggestion
import com.example.core.repo.IEventRepository
import javax.inject.Inject

class GetSearchSuggestions @Inject constructor(private val eventRepo: IEventRepository) {
    suspend operator fun invoke(
        searchText: String
    ): List<SearchSuggestion> = eventRepo.getSearchSuggestions(searchText)
}
