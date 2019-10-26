package com.example.coreandroid.util

import com.google.android.material.snackbar.Snackbar

sealed class SnackbarState {
    class Text(
        val text: String,
        @Snackbar.Duration val length: Int = Snackbar.LENGTH_INDEFINITE
    ) : SnackbarState()

    object Hidden : SnackbarState()
}