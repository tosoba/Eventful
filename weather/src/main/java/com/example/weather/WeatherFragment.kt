package com.example.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.coreandroid.util.FragmentArgument
import com.example.coreandroid.util.observe
import com.google.android.gms.maps.model.LatLng
import dagger.android.support.DaggerFragment
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
    ): View? = inflater.inflate(R.layout.fragment_weather, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.liveState.observe(this) {
            it
        }
    }
}
