package com.example.core.usecase

import com.example.core.repo.IEventRepository
import javax.inject.Inject

class SaveSuggestion @Inject constructor(private val eventRepo: IEventRepository) {
    suspend operator fun invoke(searchText: String) {
        eventRepo.saveSuggestion(searchText)
    }
}