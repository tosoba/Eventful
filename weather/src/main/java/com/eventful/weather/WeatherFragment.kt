package com.eventful.weather

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.eventful.core.android.base.DaggerViewModelFragment
import com.eventful.core.android.base.HasArgs
import com.eventful.core.android.controller.eventNavigationItemSelectedListener
import com.eventful.core.android.loadingIndicator
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.FragmentArgument
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.core.android.util.ext.*
import com.eventful.core.android.view.epoxy.EpoxyThreads
import com.eventful.core.android.view.epoxy.typedController
import com.eventful.weather.databinding.FragmentWeatherBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.material.TabLayoutSelectionEvent
import reactivecircus.flowbinding.material.tabSelectionEvents
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class WeatherFragment :
    DaggerViewModelFragment<WeatherViewModel>(R.layout.fragment_weather),
    HasArgs {

    private var event: Event by FragmentArgument(WeatherArgs.EVENT.name)
    private var bottomNavItemsToRemove: IntArray by FragmentArgument()
    override val args: Bundle get() = bundleOf(WeatherArgs.EVENT.name to event)

    private val binding: FragmentWeatherBinding by viewBinding(FragmentWeatherBinding::bind)

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

    private val epoxyController by lazy(LazyThreadSafetyMode.NONE) {
        typedController<WeatherControllerData>(epoxyThreads) { data ->
            when (data) {
                is WeatherControllerData.LoadingForecast -> loadingIndicator {
                    id("loading-indicator-weather")
                }
                is WeatherControllerData.ForecastLoaded -> {
                    val currently = data.forecast.currently
                    WeatherEpoxyModelGroup(
                        TemperatureInLocationBindingModel_()
                            .id("temperature-in-location")
                            .temperature(currently.temperature)
                            .locationInfo(
                                if (data.tab == WeatherTab.NOW) "Now in ${data.city}"
                                else "In ${data.city} at event start"
                            ),
                        WeatherSymbolInfoBindingModel_()
                            .id("weather-forecast-info")
                            .symbolResource(WeatherStatus.fromIcon(currently.icon).resource)
                            .title("Forecast"),
                        WeatherSymbolInfoBindingModel_()
                            .id("weather-wind-info")
                            .symbolResource(R.drawable.wind_info)
                            .title("Wind")
                            .info("${String.format("%.1f", currently.windSpeed)} km/h"),
                        WeatherSymbolInfoBindingModel_()
                            .id("weather-humidity-info")
                            .symbolResource(R.drawable.humidity)
                            .title("Humidity")
                            .info("${String.format("%.1f", currently.humidity * 100)}%"),
                        WeatherDescriptionBindingModel_()
                            .id("weather-description")
                            .description(currently.summary)
                    ).addTo(this)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbarWithDrawerToggle(binding.weatherToolbar)
        with(binding.weatherTabLayout) {
            WeatherTab.values().forEach { addTab(newTab().setText(it.label)) }
            tabSelectionEvents()
                .filterIsInstance<TabLayoutSelectionEvent.TabSelected>()
                .onEach {
                    viewModel.intent(WeatherIntent.TabSelected(WeatherTab.values()[it.tab.position]))
                }
                .launchIn(lifecycleScope)
        }
        binding.weatherRecyclerView.setController(epoxyController)
        with(binding.weatherBottomNavView) {
            setOnNavigationItemSelectedListener(eventNavigationItemSelectedListener)
            selectedItemId = R.id.bottom_nav_weather
            bottomNavItemsToRemove.forEach(menu::removeItem)
        }
    }

    private var viewUpdatesJob: Job? = null

    override fun onResume() {
        super.onResume()

        setupToolbar(binding.weatherToolbar)
        showBackNavArrow()
        activity?.statusBarColor = context?.themeColor(R.attr.colorPrimaryDark)

        binding.weatherBottomNavView.selectedItemId = R.id.bottom_nav_weather

        viewUpdatesJob = viewModel.viewUpdates
            .onEachLogging("VIEW_UPDATE", getString(R.string.weather)) { viewUpdate ->
                when (viewUpdate) {
                    is WeatherViewUpdate.LoadingForecast -> epoxyController.setData(
                        WeatherControllerData.LoadingForecast
                    )
                    is WeatherViewUpdate.ForecastLoaded -> epoxyController.setData(
                        WeatherControllerData.ForecastLoaded(
                            viewUpdate.forecast, viewUpdate.city, viewUpdate.tab
                        )
                    )
                    is WeatherViewUpdate.Snackbar -> snackbarController?.transitionToSnackbarState(
                        viewUpdate.state
                    )
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onPause() {
        viewUpdatesJob?.cancel()
        super.onPause()
    }

    companion object {
        fun new(
            event: Event, bottomNavItemsToRemove: IntArray
        ): WeatherFragment = WeatherFragment().also {
            it.event = event
            it.bottomNavItemsToRemove = bottomNavItemsToRemove
        }
    }
}
