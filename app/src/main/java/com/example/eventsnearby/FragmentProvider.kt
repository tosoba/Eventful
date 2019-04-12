package com.example.eventsnearby

import androidx.fragment.app.Fragment
import com.example.coreandroid.model.EventUiModel
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.event.EventFragment

object FragmentProvider : IFragmentProvider {
    override fun eventFragment(event: EventUiModel): Fragment = EventFragment.new(event)
}