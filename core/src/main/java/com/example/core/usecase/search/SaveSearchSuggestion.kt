package com.example.core.usecase.search

import com.example.core.repo.IEventRepository
import javax.inject.Inject

class SaveSearchSuggestion @Inject constructor(private val eventRepo: IEventRepository) {
    suspend operator fun invoke(searchText: String) {
        eventRepo.saveSuggestion(searchText)
    }
}
