package com.example.coreandroid.main

import androidx.lifecycle.ViewModel
import com.example.coreandroid.arch.state.ViewDataStore

class MainViewModel : ViewModel() {
    val viewStateStore = ViewDataStore(MainState.INITIAL)
}