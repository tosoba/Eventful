package com.example.search

sealed class SearchError : Throwable() {
    object NotConnected : SearchError()
}