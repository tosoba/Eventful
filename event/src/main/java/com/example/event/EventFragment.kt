package com.example.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coreandroid.model.EventUiModel
import com.example.coreandroid.util.FragmentArgument
import com.example.coreandroid.util.setupToolbarWithDrawerToggle
import com.example.coreandroid.util.showBackNavArrow
import kotlinx.android.synthetic.main.fragment_event.view.*

class EventFragment : Fragment() {

    private var event: EventUiModel by FragmentArgument()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_event, container, false).apply {
        setupToolbarWithDrawerToggle(event_toolbar)
        event_toolbar.title = event.title
        showBackNavArrow()
    }

    companion object {
        fun new(event: EventUiModel): EventFragment = EventFragment().apply {
            this.event = event
        }
    }
}
