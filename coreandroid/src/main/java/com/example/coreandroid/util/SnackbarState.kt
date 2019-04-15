package com.example.coreandroid.util

sealed class SnackbarState {
    class Loading(val message: String = "Loading in progress") : SnackbarState()
    class Info(val text: String) : SnackbarState()
    object Hidden : SnackbarState()
}