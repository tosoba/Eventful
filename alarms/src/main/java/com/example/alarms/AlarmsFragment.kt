package com.example.alarms

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.coreandroid.base.HasArgs
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.util.delegate.NullableFragmentArgument

class AlarmsFragment : Fragment(R.layout.fragment_alarms), HasArgs {

    private var event: Event? by NullableFragmentArgument()
    override val args: Bundle get() = bundleOf(EVENT_ARG_KEY to event)

    companion object {
        fun new(event: Event?): AlarmsFragment = AlarmsFragment().also {
            it.event = event
        }

        const val EVENT_ARG_KEY = "event"
    }
}
