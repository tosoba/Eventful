package com.example.coreandroid.navigation

import androidx.fragment.app.Fragment
import com.example.coreandroid.ticketmaster.Event

interface IFragmentFactory {
    fun eventFragment(event: Event): Fragment
}