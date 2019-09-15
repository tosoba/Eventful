package com.example.eventsnearby

import androidx.fragment.app.Fragment
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.ticketmaster.Event
import com.example.event.EventFragment

object FragmentProvider : IFragmentProvider {
    override fun eventFragment(event: Event): Fragment = EventFragment.new(event)
}