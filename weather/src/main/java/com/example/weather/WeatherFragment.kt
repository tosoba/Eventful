package com.example.weather

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.base.HasArgs
import com.example.coreandroid.util.delegate.NullableFragmentArgument
import com.example.coreandroid.util.delegate.viewBinding
import com.example.coreandroid.util.ext.*
import com.example.weather.databinding.FragmentWeatherBinding
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
class WeatherFragment :
    DaggerViewModelFragment<WeatherViewModel>(R.layout.fragment_weather),
    HasArgs {

    private var latLng: LatLng? by NullableFragmentArgument()
    override val args: Bundle get() = bundleOf(LAT_LNG_ARG_KEY to latLng)

    private val binding: FragmentWeatherBinding by viewBinding(FragmentWeatherBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbarWithDrawerToggle(binding.weatherToolbar)
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
                    is WeatherViewUpdate.UnknownLatLng -> {
                        // TODO: show unknown location imageView or whatever
                    }
                    is WeatherViewUpdate.ForecastLoaded -> {
                        // TODO: update forecast UI
                    }
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
        fun new(latLng: LatLng?): WeatherFragment = WeatherFragment().apply {
            this.latLng = latLng
        }

        const val LAT_LNG_ARG_KEY = "latLng"
    }
}
