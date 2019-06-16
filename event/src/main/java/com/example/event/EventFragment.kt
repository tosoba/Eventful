package com.example.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.example.coreandroid.model.EventUiModel
import com.example.coreandroid.util.FragmentArgument
import com.example.coreandroid.view.TitledFragmentsPagerAdapter
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.weather.WeatherFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event.view.*

class EventFragment : Fragment() {

    private var event: EventUiModel by FragmentArgument()

    private val eventViewPagerAdapter: FragmentPagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            childFragmentManager, arrayOf(
                "Details" to EventDetailsFragment.new(event) as Fragment,
                "Weather" to WeatherFragment.new(event.latLng!!) as Fragment
            )
        )
    }

    private val viewPagerSwipedListener = object : ViewPagerPageSelectedListener {
        override fun onPageSelected(position: Int) {
            event_bottom_nav_view.selectedItemId = viewPagerItems.inverse()[position]!!
        }
    }

    private val bottomNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_event, container, false).apply {
        event_fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        event_bottom_nav_view.setOnNavigationItemSelectedListener(bottomNavigationItemSelectedListener)

        event_view_pager.adapter = eventViewPagerAdapter
        event_view_pager.addOnPageChangeListener(viewPagerSwipedListener)
        event_view_pager.offscreenPageLimit = 2
    }


    companion object {
        fun new(event: EventUiModel): EventFragment = EventFragment().apply {
            this.event = event
        }
    }
}
