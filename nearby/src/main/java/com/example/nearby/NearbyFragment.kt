package com.example.nearby

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.coreandroid.base.InjectableVectorFragment
import com.example.coreandroid.di.Dependencies
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.util.SnackbarState
import com.example.coreandroid.util.ext.navigationFragment
import com.example.coreandroid.util.ext.restoreScrollPosition
import com.example.coreandroid.util.ext.saveScrollPosition
import com.example.coreandroid.util.ext.snackbarController
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import com.example.coreandroid.view.epoxy.listItem
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
    internal lateinit var handler: NearbyViewEventHandler

    @Inject
    @field:Named(Dependencies.EPOXY_DIFFER)
    internal lateinit var differ: Handler

    @Inject
    @field:Named(Dependencies.EPOXY_BUILDER)
    internal lateinit var builder: Handler

    private val epoxyController by lazy {
        itemListController(
            builder, differ, handler.viewModel, NearbyState::events,
            { handler.eventOccurred(Interaction.ReloadClicked) },
            EndlessRecyclerViewScrollListener {
                handler.eventOccurred(Interaction.EventListScrolledToEnd)
            }
        ) { event ->
            event.listItem(View.OnClickListener {
                handler.eventOccurred(Interaction.EventClicked(event))
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nearby, container, false).apply {
        this.nearby_events_recycler_view.setController(epoxyController)
        savedInstanceState?.let {
            this.nearby_events_recycler_view.restoreScrollPosition(
                savedInstanceState, epoxyController
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler.eventOccurred(
            Lifecycle.OnViewCreated(savedInstanceState != null)
        )

        fragmentScope.launch {
            handler.updates.collect {
                when (it) {
                    is InvalidateList -> {
                        if (it.hideSnackbar) snackbarController?.transition(SnackbarState.Hidden)
                        epoxyController.setData(handler.viewModel.currentState)
                    }
                    is ShowEvent -> {
                        navigationFragment?.showFragment(fragmentProvider.eventFragment(it.event))
                    }
                    is ShowSnackbarWithMsg -> {
                        snackbarController?.transition(SnackbarState.Text(it.msg))
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
        handler.eventOccurred(Lifecycle.OnDestroy)
        super.onDestroy()
    }
}

