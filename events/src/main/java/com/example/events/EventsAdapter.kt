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

@ObsoleteCoroutinesApi
class EventsAdapter(lifecycleOwner: LifecycleOwner) : CoUpdatableRecyclerViewAdapter<Event, EventsAdapter.ViewHolder>(
    lifecycleOwner,
    object : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = coDiffUtil.current[position]
        with(holder.view) {
            event_item_title_text_view.text = event.title
            event_item_desc_text_view.text = event.description
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}