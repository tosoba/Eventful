package com.eventful.core.android.navigation

import androidx.fragment.app.Fragment
import com.eventful.core.android.model.event.Event

interface IFragmentFactory {
    fun eventFragment(event: Event): Fragment
    fun alarmsFragment(event: Event?): Fragment
}
