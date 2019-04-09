package com.example.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.core.model.Event
import kotlinx.android.synthetic.main.event_item.view.*
import kotlinx.android.synthetic.main.fragment_events.view.*


class EventsFragment : Fragment() {

    private val adapter = EventsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_events, container, false).apply {
        events_recycler_view.adapter = adapter
    }

    fun updateEvents(newEvents: List<Event>) {
        adapter.update(newEvents)
    }

    class TestAdapter(val items: ArrayList<Event> = ArrayList()) : RecyclerView.Adapter<TestAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestAdapter.ViewHolder =
            TestAdapter.ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
            )

        override fun onBindViewHolder(holder: TestAdapter.ViewHolder, position: Int) {
            val event = items[position]
            with(holder.view) {
                event_item_title_text_view.text = event.title
                event_item_desc_text_view.text = event.description
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    }
}
