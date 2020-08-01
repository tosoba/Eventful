package com.eventful.core.android.navigation

import androidx.fragment.app.Fragment
import com.eventful.core.android.model.event.Event

interface IMainChildFragmentNavDestinations {
    fun eventFragment(event: Event): Fragment
}
