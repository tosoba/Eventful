package com.example.event

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.base.HasArgs
import com.example.coreandroid.controller.SnackbarController
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.controller.handleSnackbarState
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.util.delegate.FragmentArgument
import com.example.coreandroid.util.delegate.bottomNavItemSelectedViewPagerListener
import com.example.coreandroid.util.delegate.viewBinding
import com.example.coreandroid.util.delegate.viewPagerPageSelectedBottomNavListener
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.coreandroid.view.ext.hideAndShow
import com.example.coreandroid.view.titledFragmentsPagerAdapter
import com.example.event.databinding.FragmentEventBinding
import com.example.weather.WeatherFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@FlowPreview
class EventFragment :
    DaggerViewModelFragment<EventViewModel>(R.layout.fragment_event),
    SnackbarController,
    HasArgs {

    private var event: Event by FragmentArgument()
    override val args: Bundle get() = bundleOf("initialState" to event)

    private val binding: FragmentEventBinding by viewBinding(FragmentEventBinding::bind)

    private val eventViewPagerAdapter: PagerAdapter by titledFragmentsPagerAdapter {
        arrayOf(
            getString(R.string.details) to EventDetailsFragment.new(event),
            getString(R.string.weather) to WeatherFragment.new(event.venues?.firstOrNull()?.latLng)
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

            eventViewPager.adapter = eventViewPagerAdapter
            eventViewPager.addOnPageChangeListener(viewPagerSwipedListener)
            eventViewPager.offscreenPageLimit = 2

            eventFab.setOnClickListener {
                lifecycleScope.launch { viewModel.intent(EventIntent.ToggleFavourite) }
            }
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
                    is EventViewUpdate.FloatingActionButtonDrawable ->
                        binding.eventFab.updateDrawable(it.isFavourite)
                    is EventViewUpdate.FavouriteStatusSnackbar -> transitionToSnackbarState(
                        SnackbarState.Shown(
                            text = if (it.isFavourite) "Event was added to favourites"
                            else "Event was removed from favourites",
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
        fun new(event: Event): EventFragment = EventFragment().apply {
            this.event = event
        }

        private val navigationItems: BiMap<Int, Int> = HashBiMap.create<Int, Int>().apply {
            put(R.id.bottom_nav_event_details, 0)
            put(R.id.bottom_nav_weather, 1)
        }
    }
}
