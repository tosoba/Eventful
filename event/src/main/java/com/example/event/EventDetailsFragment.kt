package com.example.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.AsyncEpoxyController
import com.example.coreandroid.description
import com.example.coreandroid.eventInfo
import com.example.coreandroid.model.Event
import com.example.coreandroid.util.delegate.FragmentArgument
import com.example.coreandroid.util.ext.setupToolbar
import com.example.coreandroid.util.ext.setupToolbarWithDrawerToggle
import com.example.coreandroid.util.ext.showBackNavArrow
import com.example.coreandroid.util.ext.toPx
import com.example.coreandroid.util.simpleController
import com.example.coreandroid.view.epoxy.kindsCarousel
import com.example.event.databinding.FragmentEventDetailsBinding
import kotlinx.android.synthetic.main.fragment_event_details.*


class EventDetailsFragment : Fragment() {

    private var event: Event by FragmentArgument()

    private val epoxyController: AsyncEpoxyController by lazy(LazyThreadSafetyMode.NONE) {
        simpleController {
            event.kindsCarousel.addTo(this)
            eventInfo {
                id("${event.id}i")
                event(event)
                margin(requireContext().toPx(15f).toInt())
            }
            description {
                id("${event.id}d")
                text(event.info ?: "No details available")
                margin(requireContext().toPx(15f).toInt())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = DataBindingUtil.inflate<FragmentEventDetailsBinding>(
        inflater, R.layout.fragment_event_details, container, false
    ).apply {
        event = this@EventDetailsFragment.event
        eventDetailsRecyclerView.setController(epoxyController)
        setupToolbarWithDrawerToggle(eventDetailsToolbar)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        epoxyController.requestModelBuild()
    }

    override fun onResume() {
        super.onResume()
        event_details_toolbar?.let {
            setupToolbar(it)
            showBackNavArrow()
            it.title = event.name
        }
    }

    companion object {
        fun new(event: Event): EventDetailsFragment = EventDetailsFragment().apply {
            this.event = event
        }
    }
}
