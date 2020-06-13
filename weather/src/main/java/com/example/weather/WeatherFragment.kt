package com.example.weather

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.util.delegate.NullableFragmentArgument
import com.example.coreandroid.util.ext.setupToolbar
import com.example.coreandroid.util.ext.setupToolbarWithDrawerToggle
import com.example.coreandroid.util.ext.showBackNavArrow
import com.example.coreandroid.view.binding.viewBinding
import com.example.weather.databinding.FragmentWeatherBinding
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class WeatherFragment : DaggerViewModelFragment<WeatherViewModel>(R.layout.fragment_weather) {

    private var latLng: LatLng? by NullableFragmentArgument()

    private val binding: FragmentWeatherBinding by viewBinding(FragmentWeatherBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: move this logic to viewModel (inject latLng to VM through SavedStateHandle + create an event in case latLng == null)
        latLng?.let { lifecycleScope.launch { viewModel.intent(WeatherIntent.LoadWeather(it)) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbarWithDrawerToggle(binding.weatherToolbar)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.weatherToolbar)
        showBackNavArrow()
    }

    companion object {
        fun new(latLng: LatLng?): WeatherFragment = WeatherFragment().apply {
            this.latLng = latLng
        }
    }
}
