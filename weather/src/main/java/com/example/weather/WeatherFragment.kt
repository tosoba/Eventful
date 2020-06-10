package com.example.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.util.delegate.NullableFragmentArgument
import com.example.coreandroid.util.ext.setupToolbar
import com.example.coreandroid.util.ext.setupToolbarWithDrawerToggle
import com.example.coreandroid.util.ext.showBackNavArrow
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.android.synthetic.main.fragment_weather.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class WeatherFragment : DaggerViewModelFragment<WeatherViewModel>() {

    private var latLng: LatLng? by NullableFragmentArgument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO: move this logic to viewModel (inject latLng to VM through SavedStateHandle + create an event in case latLng == null)
        latLng?.let { lifecycleScope.launch { viewModel.intent(LoadWeather(it)) } }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_weather, container, false).apply {
        //TODO: if latLng == null show some error that no venue is available for event...
        setupToolbarWithDrawerToggle(weather_toolbar)
    }

    override fun onResume() {
        super.onResume()
        weather_toolbar?.let {
            setupToolbar(it)
            showBackNavArrow()
        }
    }

    companion object {
        fun new(latLng: LatLng?): WeatherFragment = WeatherFragment().apply {
            this.latLng = latLng
        }
    }
}
