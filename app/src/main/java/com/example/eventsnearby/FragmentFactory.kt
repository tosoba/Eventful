package com.example.eventsnearby

import androidx.fragment.app.Fragment
import com.example.coreandroid.model.Event
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.event.EventFragment

object FragmentFactory : IFragmentFactory {
    override fun eventFragment(event: Event): Fragment = EventFragment.new(event)
}
