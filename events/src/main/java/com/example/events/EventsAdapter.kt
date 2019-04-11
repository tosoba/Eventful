package com.example.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.core.model.Event
import com.example.coreandroid.view.CoUpdatableRecyclerViewAdapter
import kotlinx.android.synthetic.main.event_item.view.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

@ObsoleteCoroutinesApi
class EventsAdapter(lifecycleOwner: LifecycleOwner) : CoUpdatableRecyclerViewAdapter<Event, EventsAdapter.ViewHolder>(
    lifecycleOwner,
    object : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem == newItem
    }
) {
    private val eventClickedChannel: Channel<Event> = Channel()
    val eventClickedReceiveChannel: ReceiveChannel<Event> = eventClickedChannel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
    ).apply {
        view.setOnClickListener { event?.let { eventClickedChannel.offer(it) } }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = coDiffUtil.current[position]
        holder.event = event
        with(holder.view) {
            event_item_title_text_view.text = event.title
            event_item_desc_text_view.text = event.description
        }
    }

    class ViewHolder(val view: View, var event: Event? = null) : RecyclerView.ViewHolder(view)
}