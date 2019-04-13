package com.example.events

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.coreandroid.model.EventUiModel
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import kotlinx.android.synthetic.main.fragment_events.view.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.properties.Delegates


@ObsoleteCoroutinesApi
class EventsFragment : Fragment() {

    private val adapter by lazy(LazyThreadSafetyMode.NONE) { EventsAdapter(this, viewEventsChannel) }

    private var shouldAddScrollListener: Boolean by Delegates.notNull()
    private var scrollListenerVisibleThreshold: Int by Delegates.notNull()

    private val viewEventsChannel: Channel<EventsViewEvent> = Channel()
    val viewEventsReceiveChannel: ReceiveChannel<EventsViewEvent> = viewEventsChannel

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        activity?.obtainStyledAttributes(attrs, R.styleable.EventsFragment)?.run {
            shouldAddScrollListener = getBoolean(R.styleable.EventsFragment_shouldAddScrollListener, false)
            if (shouldAddScrollListener) {
                scrollListenerVisibleThreshold = getInt(
                    R.styleable.EventsFragment_scrollListenerVisibleThreshold,
                    EndlessRecyclerViewScrollListener.DEFAULT_VISIBLE_THRESHOLD
                )
            }
            recycle()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_events, container, false).apply {
        events_recycler_view.adapter = adapter
        if (shouldAddScrollListener)
            events_recycler_view.addOnScrollListener(object : EndlessRecyclerViewScrollListener(
                events_recycler_view.layoutManager!!, scrollListenerVisibleThreshold
            ) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                    viewEventsChannel.offer(EventListScrolledToEnd)
                }
            })
    }

    fun updateEvents(newEvents: List<EventUiModel>) {
        adapter.update(newEvents)
    }
}
