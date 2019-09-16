package com.example.coreandroid.util

sealed class SnackbarState {
    class Text(val text: String = "Loading in progress") : SnackbarState()
    object Hidden : SnackbarState()
}