package com.eventful.event

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.eventful.core.android.controller.EventNavigationController
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.FragmentArgument
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.core.android.view.titledFragmentsPagerAdapter
import com.eventful.event.databinding.FragmentEventBinding
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class EventFragment : DaggerFragment(R.layout.fragment_event), EventNavigationController {

    private var event: Event by FragmentArgument()

    private val binding: FragmentEventBinding by viewBinding(FragmentEventBinding::bind)
    override val viewPager: ViewPager get() = binding.eventViewPager

    @Inject
    internal lateinit var childFragmentsFactory: IEventChildFragmentsFactory

    private val eventViewPagerAdapter: PagerAdapter by titledFragmentsPagerAdapter {
        val venue = event.venues?.firstOrNull()
        arrayOf(
            getString(R.string.details) to childFragmentsFactory.eventDetailsFragment(event),
            getString(R.string.weather) to childFragmentsFactory.weatherFragment(
                venue?.latLng,
                venue?.city
            ),
            getString(R.string.alarms) to childFragmentsFactory.eventAlarmsFragment(event)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.eventViewPager) {
            adapter = eventViewPagerAdapter
            offscreenPageLimit = eventViewPagerAdapter.count - 1
        }
    }

    companion object {
        fun new(event: Event): EventFragment = EventFragment().also {
            it.event = event
        }
    }
}
