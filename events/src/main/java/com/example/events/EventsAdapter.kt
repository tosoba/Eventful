package com.example.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.coreandroid.model.EventUiModel
import com.example.coreandroid.view.CoUpdatableRecyclerViewAdapter
import kotlinx.android.synthetic.main.event_item.view.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

@ObsoleteCoroutinesApi
class EventsAdapter(lifecycleOwner: LifecycleOwner) :
    CoUpdatableRecyclerViewAdapter<EventUiModel, EventsAdapter.ViewHolder>(
    lifecycleOwner,
        object : DiffUtil.ItemCallback<EventUiModel>() {
            override fun areItemsTheSame(oldItem: EventUiModel, newItem: EventUiModel): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: EventUiModel, newItem: EventUiModel): Boolean = oldItem == newItem
    }
) {
    private val eventClickedChannel: Channel<EventUiModel> = Channel()
    val eventClickedReceiveChannel: ReceiveChannel<EventUiModel> = eventClickedChannel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
    ).apply {
        itemView.setOnClickListener { event?.let { eventClickedChannel.offer(it) } }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(coDiffUtil.current[position])
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var event: EventUiModel? = null
            private set

        fun onBind(event: EventUiModel) {
            this.event = event
            with(itemView) {
                event_item_title_text_view.text = event.title
                event_item_desc_text_view.text = event.description
            }
        }
    }
}