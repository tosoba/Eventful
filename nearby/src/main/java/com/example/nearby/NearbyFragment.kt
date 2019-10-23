package com.example.nearby

import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Toast
import com.example.coreandroid.base.InjectableVectorFragment
import com.example.coreandroid.di.Dependencies
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.util.SnackbarState
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import com.example.coreandroid.view.ToolbarActionModeCallback
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

    private val eventsScrollListener: EndlessRecyclerViewScrollListener by lazy {
        EndlessRecyclerViewScrollListener {
            handler.eventOccurred(Interaction.EventListScrolledToEnd)
        }
    }

    private val epoxyController by lazy {
        itemListController(
            builder, differ, handler.viewModel, NearbyState::events,
            onScrollListener = eventsScrollListener
        ) { selectable ->
            selectable.listItem(
                selected = selectable.selected,
                clicked = View.OnClickListener {
                    handler.eventOccurred(Interaction.EventClicked(selectable.item))
                },
                longClicked = View.OnLongClickListener {
                    handler.eventOccurred(Interaction.EventLongClicked(selectable.item))
                    true
                }
            )
        }
    }

    private var actionMode: ActionMode? = null

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
        setHasOptionsMenuIfVisible()

        handler.eventOccurred(
            Lifecycle.OnViewCreated(savedInstanceState != null)
        )

        fragmentScope.launch {
            handler.updates.collect {
                when (it) {
                    is InvalidateList -> {
                        if (it.hideSnackbar) snackbarController?.transitionTo(
                            SnackbarState.Hidden, this@NearbyFragment
                        )
                        epoxyController.setData(handler.viewModel.currentState)
                        updateActionMode()
                    }
                    is ShowEvent -> {
                        navigationFragment?.showFragment(fragmentProvider.eventFragment(it.event))
                    }
                    is ShowSnackbarAndInvalidateList -> {
                        snackbarController?.transitionTo(
                            SnackbarState.Text(it.msg), this@NearbyFragment
                        )
                        epoxyController.setData(handler.viewModel.currentState)
                        if (it.errorOccurred) eventsScrollListener.onLoadingError()
                    }
                    is FinishActionModeWithMsg -> {
                        finishActionMode()
                        Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
                    }
                    is FragmentSelectedStateChanged -> {
                        setHasOptionsMenu(it.isSelected)
                        if (it.isSelected) {
                            updateActionMode()
                            activity?.invalidateOptionsMenu()
                        } else finishActionMode()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuController?.menuView?.menu?.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nearby_events_recycler_view?.saveScrollPosition(outState)
    }

    override fun onDestroy() {
        handler.eventOccurred(Lifecycle.OnDestroy)
        super.onDestroy()
    }

    private fun finishActionMode() {
        actionMode?.finish()
        actionMode = null
    }

    private fun updateActionMode() {
        val numberOfSelectedEvents = handler.viewModel.currentState.events.value
            .filter { selectable -> selectable.selected }
            .size

        if (actionMode == null && numberOfSelectedEvents > 0) {
            actionMode = activity?.startActionMode(
                ToolbarActionModeCallback(
                    R.menu.nearby_events_selection_menu,
                    mapOf(
                        R.id.nearby_action_add_favourite to {
                            handler.eventOccurred(Interaction.AddToFavouritesClicked)
                        },
                        R.id.nearby_action_clear_selection to {
                            handler.eventOccurred(Interaction.ClearSelectionClicked)
                        }
                    )
                )
            )?.apply { title = "$numberOfSelectedEvents selected" }
        } else if (actionMode != null) {
            if (numberOfSelectedEvents > 0) actionMode?.title = "$numberOfSelectedEvents selected"
            else finishActionMode()
        }
    }
}
