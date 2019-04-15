package com.example.coreandroid.util

sealed class SnackbarContent {
    class Loading(val message: String = "Loading in progress") : SnackbarContent()
    class Info(val text: String) : SnackbarContent()
}