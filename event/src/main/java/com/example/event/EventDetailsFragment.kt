package com.example.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.example.coreandroid.description
import com.example.coreandroid.eventInfo
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.delegate.FragmentArgument
import com.example.coreandroid.util.ext.setupToolbarWithDrawerToggle
import com.example.coreandroid.util.ext.showBackNavArrow
import com.example.coreandroid.util.ext.toPx
import com.example.coreandroid.util.simpleController
import com.example.coreandroid.view.epoxy.kindsCarousel
import com.example.event.databinding.FragmentEventDetailsBinding
import com.haroldadmin.vector.VectorFragment
import kotlinx.android.synthetic.main.fragment_event_details.*


class EventDetailsFragment : VectorFragment() {

    private var event: Event by FragmentArgument()

    private val epoxyController by lazy(LazyThreadSafetyMode.NONE) {
        simpleController {
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
            event.kindsCarousel.addTo(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = DataBindingUtil.inflate<FragmentEventDetailsBinding>(
        inflater, R.layout.fragment_event_details, container, false
    ).apply {
        event = this@EventDetailsFragment.event
        eventDetailsToolbar.setup()
        eventDetailsRecyclerView.setController(epoxyController)
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        epoxyController.requestModelBuild()
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
