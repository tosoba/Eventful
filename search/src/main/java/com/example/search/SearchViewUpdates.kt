package com.example.search

import android.database.Cursor
import com.example.coreandroid.ticketmaster.Event

sealed class SearchViewUpdate

data class InvalidateList(val hideSnackbar: Boolean) : SearchViewUpdate()

data class ShowEvent(val event: Event) : SearchViewUpdate()

data class ShowSnackbarAndInvalidateList(val msg: String, val errorOccurred: Boolean) :
    SearchViewUpdate()

data class UpdateSearchSuggestions(val cursor: Cursor) : SearchViewUpdate()

data class FragmentSelectedStateChanged(val isSelected: Boolean) : SearchViewUpdate()