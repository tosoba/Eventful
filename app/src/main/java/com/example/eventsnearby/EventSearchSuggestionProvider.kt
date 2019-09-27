package com.example.eventsnearby

import android.content.SearchRecentSuggestionsProvider

class EventSearchSuggestionProvider : SearchRecentSuggestionsProvider() {

    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.example.eventsnearby"
        const val MODE = DATABASE_MODE_QUERIES
    }
}