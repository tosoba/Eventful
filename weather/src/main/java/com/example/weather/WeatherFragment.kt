package com.example.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.util.delegate.NullableFragmentArg
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
import javax.inject.Inject
import javax.inject.Provider

@FlowPreview
@ExperimentalCoroutinesApi
class WeatherFragment @Inject constructor(
    viewModelProvider: Provider<WeatherViewModel>
) : DaggerViewModelFragment<WeatherViewModel>(viewModelProvider, R.layout.fragment_weather) {

    private val latLng: LatLng? by NullableFragmentArg()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: move this logic to viewModel (inject latLng to VM through SavedStateHandle + create an event in case latLng == null)
        latLng?.let { lifecycleScope.launch { viewModel.intent(WeatherIntent.LoadWeather(it)) } }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = super.onCreateView(inflater, container, savedInstanceState).apply {
        // TODO: if latLng == null show some error that no venue is available for event...
        setupToolbarWithDrawerToggle(weather_toolbar)
    }

    override fun onResume() {
        super.onResume()
        weather_toolbar?.let {
            setupToolbar(it)
            showBackNavArrow()
        }
    }
}
