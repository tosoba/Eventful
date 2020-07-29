package com.eventful.event

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import com.eventful.alarms.AlarmsFragment
import com.eventful.alarms.AlarmsMode
import com.eventful.core.android.base.DaggerViewModelFragment
import com.eventful.core.android.base.HasArgs
import com.eventful.core.android.controller.SnackbarController
import com.eventful.core.android.controller.SnackbarState
import com.eventful.core.android.controller.handleSnackbarState
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.FragmentArgument
import com.eventful.core.android.util.delegate.bottomNavItemSelectedViewPagerListener
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.core.android.util.delegate.viewPagerPageSelectedBottomNavListener
import com.eventful.core.android.view.ViewPagerPageSelectedListener
import com.eventful.core.android.view.ext.hideAndShow
import com.eventful.core.android.view.titledFragmentsPagerAdapter
import com.eventful.event.databinding.FragmentEventBinding
import com.eventful.event.details.EventDetailsFragment
import com.eventful.weather.WeatherFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.viewpager.pageSelections

@ExperimentalCoroutinesApi
@FlowPreview
class EventFragment :
    DaggerViewModelFragment<EventViewModel>(R.layout.fragment_event),
    SnackbarController,
    HasArgs {

    private var event: Event by FragmentArgument()
    override val args: Bundle get() = bundleOf(EVENT_ARG_KEY to event)

    private val binding: FragmentEventBinding by viewBinding(FragmentEventBinding::bind)

    private val eventViewPagerAdapter: PagerAdapter by titledFragmentsPagerAdapter {
        val venue = event.venues?.firstOrNull()
        arrayOf(
            getString(R.string.details) to EventDetailsFragment.new(event),
            getString(R.string.weather) to WeatherFragment.new(venue?.latLng, venue?.city),
            getString(R.string.alarms) to AlarmsFragment.new(AlarmsMode.SingleEvent(event))
        )
    }

    private val viewPagerSwipedListener: ViewPagerPageSelectedListener
            by viewPagerPageSelectedBottomNavListener(navigationItems.inverse()) { binding.eventBottomNavView }

    private val bottomNavItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener
            by bottomNavItemSelectedViewPagerListener(navigationItems) { binding.eventViewPager }

    private lateinit var snackbarStateChannel: SendChannel<SnackbarState>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            eventBottomNavView.setOnNavigationItemSelectedListener(bottomNavItemSelectedListener)

            with(eventViewPager) {
                adapter = eventViewPagerAdapter
                addOnPageChangeListener(viewPagerSwipedListener)
                offscreenPageLimit = eventViewPagerAdapter.count - 1
                pageSelections()
                    .skipInitialValue()
                    .distinctUntilChanged()
                    .debounce(500)
                    .onEach { position -> if (position == 0) eventFab.show() else eventFab.hide() }
                    .launchIn(lifecycleScope)
            }

            eventFab.clicks()
                .onEach { viewModel.intent(EventIntent.ToggleFavourite) }
                .launchIn(lifecycleScope)

            snackbarStateChannel = handleSnackbarState(eventFab)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackbarStateChannel.close()
    }

    override fun onResume() {
        super.onResume()
        viewModel.viewUpdates
            .onEach {
                when (it) {
                    is EventViewUpdate.FloatingActionButtonDrawable -> binding.eventFab
                        .updateDrawable(it.isFavourite)
                    is EventViewUpdate.FavouriteStatusSnackbar -> transitionToSnackbarState(
                        SnackbarState.Shown(
                            text = if (it.isFavourite) getString(R.string.event_added)
                            else getString(R.string.event_removed),
                            length = Snackbar.LENGTH_SHORT
                        )
                    )
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun transitionToSnackbarState(newState: SnackbarState) {
        if (!snackbarStateChannel.isClosedForSend) snackbarStateChannel.offer(newState)
    }

    private fun FloatingActionButton.updateDrawable(isFavourite: Boolean) {
        context?.let {
            setImageDrawable(
                ContextCompat.getDrawable(
                    it,
                    if (isFavourite) R.drawable.delete else R.drawable.favourite
                )
            )
            hideAndShow()
        }
    }

    companion object {
        fun new(event: Event): EventFragment = EventFragment().also {
            it.event = event
        }

        private val navigationItems: BiMap<Int, Int> = HashBiMap.create<Int, Int>().apply {
            put(R.id.bottom_nav_event_details, 0)
            put(R.id.bottom_nav_weather, 1)
            put(R.id.bottom_nav_alarms, 2)
        }

        const val EVENT_ARG_KEY = "event"
    }
}
