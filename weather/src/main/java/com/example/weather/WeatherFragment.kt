package com.example.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.example.coreandroid.util.FragmentArgument
import com.example.coreandroid.util.observeUsing
import com.example.coreandroid.util.setupToolbarWithDrawerToggle
import com.example.coreandroid.util.showBackNavArrow
import com.google.android.gms.maps.model.LatLng
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_weather.*
import kotlinx.android.synthetic.main.fragment_weather.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class WeatherFragment : DaggerFragment(), CoroutineScope {

    private val supervisorJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + supervisorJob

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.liveState.observeUsing(this) {
            it
        }
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
