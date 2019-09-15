package com.example.nearby

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.coreandroid.SimpleEventItemBindingModel_
import com.example.coreandroid.base.InjectableVectorFragment
import com.example.coreandroid.di.Dependencies
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.util.*
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import kotlinx.android.synthetic.main.fragment_nearby.*
import kotlinx.android.synthetic.main.fragment_nearby.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


class NearbyFragment : InjectableVectorFragment() {

    @Inject
    internal lateinit var fragmentProvider: IFragmentProvider

    @Inject
    internal lateinit var eventHandler: NearbyViewEventHandler

    @Inject
    @field:Named(Dependencies.EPOXY_DIFFER)
    internal lateinit var differ: Handler

    @Inject
    @field:Named(Dependencies.EPOXY_BUILDER)
    internal lateinit var builder: Handler

    private val epoxyController by lazy {
        itemListController(
            builder, differ, eventHandler.viewModel, NearbyState::events,
            EndlessRecyclerViewScrollListener {
                eventHandler.eventOccurred(Interaction.EventListScrolledToEnd)
            },
            {}
        ) { event ->
            SimpleEventItemBindingModel_()
                .id(event.id)
                .event(event)
                .eventClicked { _ -> eventHandler.eventOccurred(Interaction.EventClicked(event)) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nearby, container, false).apply {
        this.nearby_events_recycler_view.setController(epoxyController)
        savedInstanceState?.let {
            this.nearby_events_recycler_view.restoreScrollPosition(
                savedInstanceState,
                epoxyController
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventHandler.eventOccurred(
            Lifecycle.OnViewCreated(savedInstanceState != null)
        )

        fragmentScope.launch {
            eventHandler.updates.collect {
                when (it) {
                    is UpdateEvents -> {
                        snackbarController?.transition(SnackbarState.Hidden)
                        epoxyController.setData(eventHandler.viewModel.currentState)
                    }
                    is ShowEvent -> {
                        navigationFragment?.showFragment(fragmentProvider.eventFragment(it.event))
                    }
                    is ShowNoConnectionMessage -> {
                        //TODO
                        Log.e("CON", "No connection.")
                    }
                    is ShowLocationUnavailableMessage -> {
                        //TODO
                        Log.e("LOC", "Location unavailable.")
                    }
                    is ShowLoadingSnackbar -> {
                        snackbarController?.transition(SnackbarState.Loading())
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nearby_events_recycler_view?.saveScrollPosition(outState)
    }

    override fun onDestroy() {
        eventHandler.eventOccurred(Lifecycle.OnDestroy)
        super.onDestroy()
    }
}

