package com.eventful.core.usecase.search

import com.eventful.core.repo.IEventRepository
import javax.inject.Inject

class SaveSearchSuggestion @Inject constructor(private val eventRepo: IEventRepository) {
    suspend operator fun invoke(searchText: String) {
        eventRepo.saveSuggestion(searchText)
    }
}
