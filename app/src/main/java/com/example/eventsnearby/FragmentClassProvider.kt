package com.example.eventsnearby

import androidx.fragment.app.Fragment
import com.example.coreandroid.navigation.EventFragmentClassProvider
import com.example.event.EventFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
object FragmentClassProvider : EventFragmentClassProvider {
    override val eventFragmentClass: Class<out Fragment> get() = EventFragment::class.java
}
