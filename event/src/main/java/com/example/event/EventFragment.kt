package com.example.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.base.HasArgs
import com.example.coreandroid.controller.SnackbarController
import com.example.coreandroid.controller.SnackbarState
import com.example.coreandroid.controller.handleSnackbarState
import com.example.coreandroid.model.Event
import com.example.coreandroid.util.delegate.FragmentArg
import com.example.coreandroid.util.delegate.FragmentArgument
import com.example.coreandroid.view.TitledFragmentData
import com.example.coreandroid.view.TitledFragmentsPagerAdapter
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.coreandroid.view.ext.hideAndShow
import com.example.weather.WeatherFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@ExperimentalCoroutinesApi
@FlowPreview
class EventFragment @Inject constructor(
    viewModelProvider: Provider<EventViewModel>
) : DaggerViewModelFragment<EventViewModel>(viewModelProvider, R.layout.fragment_event),
    SnackbarController,
    HasArgs {

    private val event: Event by FragmentArg()
    override val args: Bundle get() = bundleOf("initialState" to event)

    private val eventViewPagerAdapter: PagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            requireContext().classLoader,
            childFragmentManager,
            arrayOf(
                TitledFragmentData(
                    EventDetailsFragment::class.java,
                    "Details",
                    bundleOf("event" to event)
                ),
                TitledFragmentData(
                    WeatherFragment::class.java,
                    "Weather",
                    bundleOf("latLng" to event.venues?.firstOrNull()?.latLng)
                )
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

    private lateinit var snackbarStateChannel: SendChannel<SnackbarState>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = super.onCreateView(inflater, container, savedInstanceState).apply {
        event_fab.setOnClickListener {
            lifecycleScope.launch { viewModel.intent(EventIntent.ToggleFavourite) }
        }

        event_bottom_nav_view.setOnNavigationItemSelectedListener(bottomNavItemSelectedListener)

        event_view_pager.adapter = eventViewPagerAdapter
        event_view_pager.addOnPageChangeListener(viewPagerSwipedListener)
        event_view_pager.offscreenPageLimit = 2

        snackbarStateChannel = handleSnackbarState(event_fab)
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
                    is EventViewUpdate.FloatingActionButtonDrawable -> event_fab?.updateDrawable(
                        it.isFavourite
                    )
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
        private val viewPagerItems: BiMap<Int, Int> = HashBiMap.create<Int, Int>().apply {
            put(R.id.bottom_nav_event_details, 0)
            put(R.id.bottom_nav_weather, 1)
        }
    }
}
