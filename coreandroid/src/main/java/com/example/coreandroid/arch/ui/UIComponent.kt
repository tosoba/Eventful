package com.example.coreandroid.arch.ui

import kotlinx.coroutines.channels.ReceiveChannel

interface UIComponent<T> {
    fun getContainerId(): Int
    fun getUserInteractionEvents(): ReceiveChannel<T>
}