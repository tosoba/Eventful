package com.eventful.event

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.eventful.core.android.base.DaggerViewModelFragment
import com.eventful.core.android.base.HasArgs
import com.eventful.core.android.controller.EventNavigationController
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.FragmentArgument
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.core.android.view.titledFragmentsPagerAdapter
import com.eventful.event.databinding.FragmentEventBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class EventFragment :
    DaggerViewModelFragment<EventViewModel>(R.layout.fragment_event),
    EventNavigationController,
    HasArgs {

    private var event: Event by FragmentArgument()
    override val args: Bundle get() = bundleOf(EVENT_ARG_KEY to event)

    private val binding: FragmentEventBinding by viewBinding(FragmentEventBinding::bind)
    override val viewPager: ViewPager get() = binding.eventViewPager

    @Inject
    internal lateinit var fragmentsFactory: IEventChildFragmentsFactory

    private val eventViewPagerAdapter: PagerAdapter by titledFragmentsPagerAdapter {
        val details = getString(R.string.details) to fragmentsFactory.eventDetailsFragment(
            event,
            !event.startDateTimeSetInFuture
        )
        val venue = event.venues?.firstOrNull()
        val weather = getString(R.string.weather) to fragmentsFactory.weatherFragment(
            venue?.latLng,
            venue?.city,
            !event.startDateTimeSetInFuture
        )
        if (event.startDateTimeSetInFuture) {
            arrayOf(
                details,
                weather,
                getString(R.string.alarms) to fragmentsFactory.eventAlarmsFragment(event)
            )
        } else {
            arrayOf(details, weather)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.eventViewPager) {
            adapter = eventViewPagerAdapter
            offscreenPageLimit = eventViewPagerAdapter.count - 1
        }
    }

    companion object {
        const val EVENT_ARG_KEY = "event"

        fun new(event: Event): EventFragment = EventFragment().also {
            it.event = event
        }
    }
}
