package com.example.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.coreandroid.model.EventUiModel
import com.example.coreandroid.util.BannerSliderAdapter
import com.example.coreandroid.view.CoUpdatableRecyclerViewAdapter
import com.example.events.databinding.EventItemBinding
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel

@ObsoleteCoroutinesApi
class EventsAdapter(
    lifecycleOwner: LifecycleOwner, private val viewEventsChannel: SendChannel<EventsViewEvent>
) : CoUpdatableRecyclerViewAdapter<EventUiModel, EventsAdapter.ViewHolder>(
    lifecycleOwner,
    object : DiffUtil.ItemCallback<EventUiModel>() {
        override fun areItemsTheSame(oldItem: EventUiModel, newItem: EventUiModel): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: EventUiModel, newItem: EventUiModel): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        DataBindingUtil.inflate<EventItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.event_item,
            parent,
            false
        ).apply {
            sliderAdapter = BannerSliderAdapter(emptyList())
        }
    ).apply {
        itemView.setOnClickListener { binding.event?.let { viewEventsChannel.offer(EventClicked(it)) } }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = coDiffUtil.current[position]
        holder.binding.event = event
        if (event.photoUrls.isNotEmpty()) {
            holder.binding.sliderAdapter = BannerSliderAdapter(event.photoUrls)
        } else {
            event.onPhotoUrlsAdded = {
                holder.binding.sliderAdapter = BannerSliderAdapter(event.photoUrls)
            }
        }
    }

    class ViewHolder(val binding: EventItemBinding) : RecyclerView.ViewHolder(binding.root)
}