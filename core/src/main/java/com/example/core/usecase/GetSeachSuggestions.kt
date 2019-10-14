package com.example.core.usecase

import com.example.core.model.search.SearchSuggestion
import com.example.core.repo.IEventRepository
import javax.inject.Inject

class GetSeachSuggestions @Inject constructor(private val eventRepo: IEventRepository) {
    suspend operator fun invoke(
        searchText: String
    ): List<SearchSuggestion> = eventRepo.getSearchSuggestions(searchText)
}