package com.example.coreandroid.util

//TODO: Loading snackbars are a dumb idea (espacially for loading more items)
sealed class SnackbarState {
    class Text(val text: String = "Loading in progress") : SnackbarState()
    object Hidden : SnackbarState()
}