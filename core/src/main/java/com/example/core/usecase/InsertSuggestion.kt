package com.example.core.usecase

import com.example.core.repo.IEventsRepository
import javax.inject.Inject

class InsertSuggestion @Inject constructor(private val eventsRepo: IEventsRepository) {
    suspend operator fun invoke(searchText: String) {
        eventsRepo.insertSuggestion(searchText)
    }
}