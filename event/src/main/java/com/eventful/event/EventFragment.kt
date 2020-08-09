package com.eventful.event

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.eventful.core.android.base.BackPressedHandler
import com.eventful.core.android.base.DaggerViewModelFragment
import com.eventful.core.android.base.HasArgs
import com.eventful.core.android.controller.EventNavigationController
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.FragmentArgument
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.core.android.util.ext.navigationFragment
import com.eventful.core.android.view.titledFragmentsPagerAdapter
import com.eventful.event.databinding.FragmentEventBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class EventFragment :
    DaggerViewModelFragment<EventViewModel>(R.layout.fragment_event),
    EventNavigationController,
    HasArgs,
    BackPressedHandler {

    private var event: Event by FragmentArgument(EventArgs.EVENT.name)
    override val args: Bundle get() = bundleOf(EventArgs.EVENT.name to event)

    private val binding: FragmentEventBinding by viewBinding(FragmentEventBinding::bind)
    override val viewPager: ViewPager get() = binding.eventViewPager

    @Inject
    internal lateinit var fragmentsFactory: IEventChildFragmentsFactory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.viewUpdates
            .onEach { update ->
                when (update) {
                    is EventViewUpdate.Pop -> navigationFragment?.popBackStack()
                    is EventViewUpdate.NewViewPager -> with(binding.eventViewPager) {
                        val eventViewPagerAdapter: PagerAdapter by titledFragmentsPagerAdapter {
                            update.fragments
                        }
                        adapter = eventViewPagerAdapter
                        offscreenPageLimit = eventViewPagerAdapter.count - 1
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    private val EventViewUpdate.NewViewPager.fragments: Array<Pair<String, Fragment>>
        get() = sequenceOf(
            getString(R.string.details) to fragmentsFactory.eventDetailsFragment(
                event, bottomNavItemsToRemove
            ),
            if (includeWeather) getString(R.string.weather) to fragmentsFactory.weatherFragment(
                event, bottomNavItemsToRemove
            ) else null,
            if (includeAlarms) getString(R.string.alarms) to fragmentsFactory.eventAlarmsFragment(
                event, bottomNavItemsToRemove
            ) else null
        ).filterNotNull().toList().toTypedArray()

    private val EventViewUpdate.NewViewPager.bottomNavItemsToRemove: IntArray
        get() = when {
            !includeWeather && !includeAlarms -> intArrayOf(
                R.id.bottom_nav_weather,
                R.id.bottom_nav_alarms
            )
            !includeWeather && includeAlarms -> intArrayOf(R.id.bottom_nav_weather)
            includeWeather && !includeAlarms -> intArrayOf(R.id.bottom_nav_alarms)
            includeWeather && includeAlarms -> intArrayOf()
            else -> throw IllegalArgumentException()
        }

    override fun showEventDetails(event: Event) {
        viewPager.currentItem =
            EventNavigationController.navigationItems[R.id.bottom_nav_event_details]
                ?: throw IllegalStateException()
        lifecycleScope.launch { viewModel.intent(EventIntent.NewEvent(event)) }
    }

    override fun onBackPressed() {
        lifecycleScope.launch { viewModel.intent(EventIntent.BackPressed) }
    }

    companion object {
        fun new(event: Event): EventFragment = EventFragment().also {
            it.event = event
        }
    }
}
