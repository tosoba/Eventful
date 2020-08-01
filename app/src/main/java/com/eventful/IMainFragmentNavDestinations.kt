package com.eventful

import com.eventful.alarms.AlarmsFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
interface IMainFragmentNavDestinations {
    val alarmsFragment: AlarmsFragment
}
