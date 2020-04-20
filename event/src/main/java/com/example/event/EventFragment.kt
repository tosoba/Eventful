package com.example.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import com.example.coreandroid.base.InjectableFragment
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.util.*
import com.example.coreandroid.util.delegate.FragmentArgument
import com.example.coreandroid.view.TitledFragmentsPagerAdapter
import com.example.coreandroid.view.ViewPagerPageSelectedListener
import com.example.weather.WeatherFragment
import com.github.satoshun.coroutinebinding.view.clicks
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class EventFragment : InjectableFragment() {

    var event: Event by FragmentArgument()
        private set

    private val eventViewPagerAdapter: PagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(
            childFragmentManager, arrayOf(
                "Details" to EventDetailsFragment.new(event) as Fragment,
                "Weather" to WeatherFragment.new(event.venues?.firstOrNull()?.latLng) as Fragment
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

    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_event, container, false).apply {
        lifecycleScope.launch {
            event_fab.clicks()
                .consumeAsFlow()
                .debounce(200)
                .collect { viewModel.toggleEventFavourite() }
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

        lifecycleScope.launch {
            viewModel.state.map { it.isFavourite }
                .scan(emptyList<Data<Boolean>>()) { previousStates, value ->
                    if (previousStates.size < 3) previousStates + value
                    else listOf(previousStates[1], previousStates[2], value)
                }
                .drop(1)
                .collect { states ->
                    val (isFavourite, status) = states.last()
                    when (status) {
                        is Initial, Loading -> {
                            event_fab?.hide()
                        }
                        is LoadedSuccessfully -> {
                            event_fab?.updateDrawableAndShow(isFavourite)
                            if (states.dropLast(1).filter { it.status is LoadedSuccessfully }.any()) updateSnackbar(
                                SnackbarState.Text(
                                    text = if (isFavourite) "Event: ${event.name} was added to favourites"
                                    else "Event: ${event.name} was removed from favourites",
                                    length = Snackbar.LENGTH_SHORT
                                )
                            )
                        }
                        is LoadingFailed<*> -> {
                            event_fab?.updateDrawableAndShow(isFavourite)
                            updateSnackbar(
                                SnackbarState.Text(
                                    text = """Error occurred when trying to 
                                    |${if (isFavourite) "remove" else "add"} event: ${event.name} 
                                    |to favourites""".trimMargin(),
                                    length = Snackbar.LENGTH_SHORT
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun updateSnackbar(snackbarState: SnackbarState) {
        event_fab?.let {
            snackbar = when (snackbarState) {
                is SnackbarState.Text -> {
                    snackbar?.dismiss()
                    Snackbar.make(it, snackbarState.text, snackbarState.length)
                        .apply(Snackbar::show)
                }
                is SnackbarState.Hidden -> {
                    snackbar?.dismiss()
                    null
                }
            }
        }
    }

    private fun FloatingActionButton.updateDrawableAndShow(isFavourite: Boolean) {
        context?.let {
            setImageDrawable(
                ContextCompat.getDrawable(
                    it,
                    if (isFavourite) R.drawable.delete else R.drawable.favourite
                )
            )
        }
        show()
    }

    companion object {
        fun new(event: Event): EventFragment = EventFragment().apply {
            this.event = event
        }
    }
}
