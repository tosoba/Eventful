package com.example.search

import android.database.Cursor
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.SnackbarState

sealed class SearchViewUpdate

data class InvalidateList(val errorOccurred: Boolean) : SearchViewUpdate()

data class ShowEvent(val event: Event) : SearchViewUpdate()

data class UpdateSnackbar(val state: SnackbarState) : SearchViewUpdate()

data class UpdateSearchSuggestions(val cursor: Cursor) : SearchViewUpdate()
