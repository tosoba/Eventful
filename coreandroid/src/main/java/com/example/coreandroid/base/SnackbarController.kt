package com.example.coreandroid.base

import com.example.coreandroid.util.SnackbarState

interface SnackbarController {
    fun snackbarTransition(newState: SnackbarState)
}