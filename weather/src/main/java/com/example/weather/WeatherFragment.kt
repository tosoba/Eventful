package com.example.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.example.coreandroid.base.InjectableVectorFragment
import com.example.coreandroid.util.FragmentArgument
import com.example.coreandroid.util.ext.setupToolbarWithDrawerToggle
import com.example.coreandroid.util.ext.showBackNavArrow
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.android.synthetic.main.fragment_weather.view.*
import javax.inject.Inject


class WeatherFragment : InjectableVectorFragment() {

    @Inject
    lateinit var viewModel: WeatherViewModel

    private var latLng: LatLng by FragmentArgument()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadWeather(latLng)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_weather, container, false).apply {
        weather_toolbar.setup()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && view != null) weather_toolbar.setup()
    }

    private fun Toolbar.setup() {
        setupToolbarWithDrawerToggle(this)
        showBackNavArrow()
    }

    companion object {
        fun new(latLng: LatLng): WeatherFragment = WeatherFragment().apply {
            this.latLng = latLng
        }
    }
}
