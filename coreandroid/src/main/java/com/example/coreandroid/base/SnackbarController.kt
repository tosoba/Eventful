package com.example.coreandroid.base

import com.example.coreandroid.util.SnackbarContent

interface SnackbarController {
    fun showSnackbar(content: SnackbarContent)
    fun hideSnackbar()
}