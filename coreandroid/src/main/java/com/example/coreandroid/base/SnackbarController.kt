package com.example.coreandroid.base

import androidx.fragment.app.Fragment
import com.example.coreandroid.util.SnackbarState

interface SnackbarController {
    fun transitionTo(newState: SnackbarState, fragment: Fragment)
}