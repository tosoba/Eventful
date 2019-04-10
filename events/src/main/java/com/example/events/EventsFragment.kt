package com.example.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.core.model.Event
import kotlinx.android.synthetic.main.fragment_events.view.*


class EventsFragment : Fragment() {

    private val adapter by lazy(LazyThreadSafetyMode.NONE) { EventsAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_events, container, false).apply {
        events_recycler_view.adapter = adapter
    }

    fun updateEvents(newEvents: List<Event>) {
        adapter.update(newEvents)
    }
}
