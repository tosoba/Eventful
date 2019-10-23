package com.example.event

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import com.example.coreandroid.base.InjectableVectorFragment
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.FragmentArgument
import com.example.coreandroid.util.LoadedSuccessfully
import com.example.coreandroid.view.TitledFragmentsPagerAdapter
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.weather.WeatherFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class EventFragment : InjectableVectorFragment() {

    var event: Event by FragmentArgument()
        private set

    private val eventViewPagerAdapter: PagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            childFragmentManager, listOfNotNull(
                "Details" to EventDetailsFragment.new(event) as Fragment,
                event.venues?.firstOrNull()?.run {
                    "Weather" to WeatherFragment.new(
                        LatLng(lat.toDouble(), lng.toDouble())
                    ) as Fragment
                }
            )
        )
    }

    private val viewPagerSwipedListener = object : ViewPagerPageSelectedListener {
        override fun onPageSelected(position: Int) {
            event_bottom_nav_view.selectedItemId = viewPagerItems.inverse()[position]!!
        }
    }

    private val bottomNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            viewPagerItems[item.itemId]?.let {
                event_view_pager?.currentItem = it
                return@OnNavigationItemSelectedListener true
            }
            false
        }

    private val viewPagerItems: BiMap<Int, Int> = HashBiMap.create<Int, Int>().apply {
        put(R.id.bottom_nav_event_details, 0)
        put(R.id.bottom_nav_weather, 1)
    }

    @Inject
    internal lateinit var viewModel: EventViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_event, container, false).apply {
        event_fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        event_bottom_nav_view.setOnNavigationItemSelectedListener(
            bottomNavigationItemSelectedListener
        )

        event_view_pager.adapter = eventViewPagerAdapter
        event_view_pager.addOnPageChangeListener(viewPagerSwipedListener)
        event_view_pager.offscreenPageLimit = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentScope.launch {
            viewModel.state.filter { it.isFavourite.status is LoadedSuccessfully }
                .map { it.isFavourite.value }
                .collect {
                    //TODO: show hide fab depending on it
                    Log.e("SAVED", it.toString())
                }
        }
    }

    companion object {
        fun new(event: Event): EventFragment = EventFragment().apply {
            this.event = event
        }
    }
}
