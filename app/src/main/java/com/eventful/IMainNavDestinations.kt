package com.eventful

import androidx.fragment.app.Fragment
import com.eventful.core.android.model.event.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
interface IMainNavDestinations {
    val alarmsFragment: Fragment
    fun eventFragment(event: Event): Fragment
}
