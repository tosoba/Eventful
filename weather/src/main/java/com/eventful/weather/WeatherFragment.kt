package com.eventful.weather

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.eventful.core.android.base.DaggerViewModelFragment
import com.eventful.core.android.base.HasArgs
import com.eventful.core.android.controller.eventNavigationItemSelectedListener
import com.eventful.core.android.loadingIndicator
import com.eventful.core.android.unknownLocation
import com.eventful.core.android.util.delegate.NullableFragmentArgument
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.core.android.util.ext.*
import com.eventful.core.android.view.epoxy.EpoxyThreads
import com.eventful.core.android.view.epoxy.typedController
import com.eventful.weather.databinding.FragmentWeatherBinding
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class WeatherFragment :
    DaggerViewModelFragment<WeatherViewModel>(R.layout.fragment_weather),
    HasArgs {

    private var latLng: LatLng? by NullableFragmentArgument()
    private var locationName: String? by NullableFragmentArgument()
    override val args: Bundle get() = bundleOf(LAT_LNG_ARG_KEY to latLng)

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
                            .locationName(locationName),
                        WeatherSymbolInfoBindingModel_()
                            .id("weather-forecast-info")
                            .symbolResource(WeatherStatus.fromIcon(currently.icon).resource)
                            .title("Forecast")
                            .info(currently.summary),
                        WeatherSymbolInfoBindingModel_()
                            .id("weather-wind-info")
                            .symbolResource(R.drawable.wind_info)
                            .title("Wind")
                            .info("${currently.windSpeed} km/h"),
                        WeatherSymbolInfoBindingModel_()
                            .id("weather-humidity-info")
                            .symbolResource(R.drawable.humidity)
                            .title("Humidity")
                            .info("${currently.humidity * 100}%"),
                        WeatherDescriptionBindingModel_()
                            .id("weather-description")
                            .description(currently.summary)
                    ).addTo(this)
                }
                is WeatherControllerData.UnknownLatLng -> unknownLocation {
                    id("unknown-location-weather")
                    text(getString(R.string.event_location_unknown))
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbarWithDrawerToggle(binding.weatherToolbar)
        binding.weatherRecyclerView.setController(epoxyController)
        with(binding.weatherBottomNavView) {
            setOnNavigationItemSelectedListener(eventNavigationItemSelectedListener)
            selectedItemId = R.id.bottom_nav_weather
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
            .onEach { viewUpdate ->
                when (viewUpdate) {
                    is WeatherViewUpdate.UnknownLatLng -> epoxyController.setData(
                        WeatherControllerData.UnknownLatLng
                    )
                    is WeatherViewUpdate.LoadingForecast -> epoxyController.setData(
                        WeatherControllerData.LoadingForecast
                    )
                    is WeatherViewUpdate.ForecastLoaded -> epoxyController.setData(
                        WeatherControllerData.ForecastLoaded(viewUpdate.forecast)
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
        fun new(latLng: LatLng?, locationName: String?): WeatherFragment = WeatherFragment().also {
            it.latLng = latLng
            it.locationName = locationName
        }

        const val LAT_LNG_ARG_KEY = "LAT_LNG_ARG_KEY"
    }
}
