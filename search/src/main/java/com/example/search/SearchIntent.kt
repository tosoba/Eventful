package com.example.search

sealed class SearchIntent
data class NewSearch(val text: String, val confirmed: Boolean) : SearchIntent()
object LoadMoreResults : SearchIntent()
