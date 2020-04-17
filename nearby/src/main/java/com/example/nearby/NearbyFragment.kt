package com.example.nearby

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.example.coreandroid.base.InjectableEpoxyFragment
import com.example.coreandroid.base.clearMenu
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.restoreScrollPosition
import com.example.coreandroid.util.ext.saveScrollPosition
import com.example.coreandroid.util.ext.snackbarController
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import com.example.coreandroid.view.ToolbarActionModeCallback
import com.example.coreandroid.view.epoxy.listItem
import kotlinx.android.synthetic.main.fragment_nearby.*
import kotlinx.android.synthetic.main.fragment_nearby.view.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


class NearbyFragment : InjectableEpoxyFragment() {

    @Inject
    internal lateinit var fragmentProvider: IFragmentProvider

    @Inject
    internal lateinit var viewModel: NearbyVM

    private val eventsScrollListener: EndlessRecyclerViewScrollListener by lazy {
        EndlessRecyclerViewScrollListener {
            fragmentScope.launch { viewModel.send(EventListScrolledToEnd) }
        }
    }

    private val epoxyController by lazy {
        itemListController(
            viewModel, NearbyState::events,
            onScrollListener = eventsScrollListener
        ) { selectable ->
            selectable.listItem(
                selected = selectable.selected,
                clicked = View.OnClickListener {
                    //TODO: navigate to EventFragment
                },
                longClicked = View.OnLongClickListener {
                    fragmentScope.launch {
                        viewModel.send(EventLongClicked(selectable.item))
                    }
                    true
                }
            )
        }
    }

    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

    override fun onStart() {
        super.onStart()

        viewModel.states.onEach { epoxyController.setData(it) }.launchIn(fragmentScope)

        viewModel.states
            .map { it.snackbarState }
            .distinctUntilChanged()
            .onEach { snackbarController?.transitionTo(it) }
            .launchIn(fragmentScope)

        viewModel.events.observe(this, Observer {
            if (it is NearbySignal.FavouritesSaved) finishActionMode()
        })
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
        updateActionMode()
        snackbarController?.transitionTo(viewModel.state.snackbarState)
    }

    override fun onPause() {
        super.onPause()
        finishActionMode()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuController?.clearMenu()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nearby_events_recycler_view?.saveScrollPosition(outState)
    }

    private fun finishActionMode() {
        actionMode?.finish()
        actionMode = null
    }

    private fun updateActionMode() {
        val numberOfSelectedEvents = viewModel.state.events.value
            .filter { selectable -> selectable.selected }
            .size

        if (actionMode == null && numberOfSelectedEvents > 0) {
            actionMode = activity?.startActionMode(
                ToolbarActionModeCallback(
                    R.menu.nearby_events_selection_menu,
                    mapOf(
                        R.id.nearby_action_add_favourite to {
                            fragmentScope.launch { viewModel.send(AddToFavouritesClicked) }
                            Unit
                        },
                        R.id.nearby_action_clear_selection to {
                            fragmentScope.launch { viewModel.send(ClearSelectionClicked) }
                            Unit
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
