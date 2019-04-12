package com.example.coreandroid.navigation

import androidx.fragment.app.Fragment
import com.example.coreandroid.model.EventUiModel

interface IFragmentProvider {
    fun eventFragment(event: EventUiModel): Fragment
}