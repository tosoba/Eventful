package com.example.nearby

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.util.navigationFragment
import com.example.events.EventClicked
import com.example.events.EventListScrolledToEnd
import com.example.events.EventsFragment
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class NearbyFragment : DaggerFragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + supervisorJob

    private val supervisorJob = Job()

    @Inject
    lateinit var fragmentProvider: IFragmentProvider

    private val eventsFragment: EventsFragment by lazy(LazyThreadSafetyMode.NONE) {
        childFragmentManager.findFragmentById(R.id.nearby_events_list_fragment) as EventsFragment
    }

    @Inject
    lateinit var eventHandler: NearbyViewEventHandler

    override fun onDestroy() {
        launch {
            eventHandler.viewEventsSendChannel.send(Lifecycle.OnDestroy)
        }
        supervisorJob.cancel()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nearby, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChannels()
        launch {
            eventHandler.viewEventsSendChannel.send(Lifecycle.OnViewCreated(this@NearbyFragment))
        }
    }

    private fun setupChannels() {
        launch {
            eventsFragment.viewEventsReceiveChannel.consumeEach {
                when (it) {
                    is EventClicked -> eventHandler.viewEventsSendChannel.send(Interaction.EventClicked(it.event))
                    is EventListScrolledToEnd -> eventHandler.viewEventsSendChannel.send(Interaction.EventListScrolledToEnd)
                }
            }
        }

        launch {
            eventHandler.viewUpdatesReceiveChannel.consumeEach {
                when (it) {
                    is UpdateEvents -> eventsFragment.updateEvents(it.events)
                    is ShowEvent -> {
                        navigationFragment?.showFragment(fragmentProvider.eventFragment(it.event))
                    }
                    is ShowNoConnectionMessage -> {
                        //TODO
                        Log.e("ERR", "No connection.")
                    }
                    is ShowLocationUnavailableMessage -> {
                        //TODO
                    }
                }
            }
        }
    }
}

