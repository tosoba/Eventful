package com.example.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.FragmentArgument
import com.example.coreandroid.util.ext.setupToolbarWithDrawerToggle
import com.example.coreandroid.util.ext.showBackNavArrow
import kotlinx.android.synthetic.main.fragment_event_details.*
import kotlinx.android.synthetic.main.fragment_event_details.view.*


class EventDetailsFragment : Fragment() {

    private var event: Event by FragmentArgument()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_event_details, container, false).apply {
        event_details_toolbar.setup()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && view != null) event_details_toolbar.setup()
    }

    private fun Toolbar.setup() {
        setupToolbarWithDrawerToggle(this)
        title = event.name
        showBackNavArrow()
    }

    companion object {
        fun new(event: Event): EventDetailsFragment = EventDetailsFragment().apply {
            this.event = event
        }
    }
}
