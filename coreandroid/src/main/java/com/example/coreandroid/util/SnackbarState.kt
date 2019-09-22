package com.example.coreandroid.util

sealed class SnackbarState {
    class Text(val text: String) : SnackbarState()
    object Hidden : SnackbarState()
}