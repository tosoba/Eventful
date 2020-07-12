package com.example.weather

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.base.HasArgs
import com.example.coreandroid.loadingIndicator
import com.example.coreandroid.unknownLocation
import com.example.coreandroid.util.delegate.NullableFragmentArgument
import com.example.coreandroid.util.delegate.viewBinding
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.view.epoxy.EpoxyThreads
import com.example.coreandroid.view.epoxy.typedController
import com.example.weather.databinding.FragmentWeatherBinding
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
                is WeatherControllerData.ForecastLoaded -> weatherCurrently {
                    id("weather-currently")
                    currently(data.forecast.currently)
                    locationName(locationName)
                }
                is WeatherControllerData.UnknownLatLng -> unknownLocation {
                    id("unknown-location-weather")
                    text("Event's location is unknown.")
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbarWithDrawerToggle(binding.weatherToolbar)
        binding.weatherRecyclerView.setController(epoxyController)
    }

    private var viewUpdatesJob: Job? = null

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.weatherToolbar)
        showBackNavArrow()
        activity?.statusBarColor = context?.themeColor(R.attr.colorPrimaryDark)

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
        fun new(latLng: LatLng?, locationName: String?): WeatherFragment = WeatherFragment().apply {
            this.latLng = latLng
            this.locationName = locationName
        }

        const val LAT_LNG_ARG_KEY = "LAT_LNG_ARG_KEY"
    }
}
