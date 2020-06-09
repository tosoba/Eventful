package com.example.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import com.example.core.util.LoadedSuccessfully
import com.example.coreandroid.controller.SnackbarController
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.controller.handleSnackbarState
import com.example.coreandroid.model.Event
import com.example.coreandroid.util.delegate.FragmentArgument
import com.example.coreandroid.view.TitledFragmentsPagerAdapter
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.coreandroid.view.ext.hideAndShow
import com.example.weather.WeatherFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class EventFragment : DaggerFragment(), SnackbarController {

    var event: Event by FragmentArgument()
        private set

    private val eventViewPagerAdapter: PagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            childFragmentManager, arrayOf(
                "Details" to EventDetailsFragment.new(event),
                "Weather" to WeatherFragment.new(event.venues?.firstOrNull()?.latLng)
            )
        )
    }

    private val viewPagerSwipedListener = object : ViewPagerPageSelectedListener {
        override fun onPageSelected(position: Int) {
            event_bottom_nav_view.selectedItemId = viewPagerItems.inverse()[position]!!
        }
    }

    private val bottomNavItemSelectedListener = BottomNavigationView
        .OnNavigationItemSelectedListener { item ->
            viewPagerItems[item.itemId]?.let {
                event_view_pager?.currentItem = it
                true
            } ?: false
        }

    @Inject
    internal lateinit var viewModel: EventViewModel

    private lateinit var snackbarStateChannel: SendChannel<SnackbarState>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_event, container, false).apply {
        event_fab.setOnClickListener {
            lifecycleScope.launch { viewModel.intent(ToggleFavourite) }
        }

        event_bottom_nav_view.setOnNavigationItemSelectedListener(bottomNavItemSelectedListener)

        event_view_pager.adapter = eventViewPagerAdapter
        event_view_pager.addOnPageChangeListener(viewPagerSwipedListener)
        event_view_pager.offscreenPageLimit = 2

        snackbarStateChannel = handleSnackbarState(event_fab)
    }

    override fun onResume() {
        super.onResume()

        viewModel.states
            .map { it.isFavourite }
            .distinctUntilChanged()
            .onEach { (isFavourite, status) ->
                when (status) {
                    is LoadedSuccessfully -> event_fab?.updateDrawable(isFavourite)
                }
            }
            .launchIn(lifecycleScope)

        viewModel.signals
            .onEach {
                if (it is EventSignal.FavouriteStateToggled) {
                    transitionToSnackbarState(
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

        private val viewPagerItems: BiMap<Int, Int> = HashBiMap.create<Int, Int>().apply {
            put(R.id.bottom_nav_event_details, 0)
            put(R.id.bottom_nav_weather, 1)
        }
    }
}
