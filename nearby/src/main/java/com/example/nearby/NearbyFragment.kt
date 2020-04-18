package com.example.nearby

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.example.coreandroid.base.EventsSelectionActionModeController
import com.example.coreandroid.base.InjectableEpoxyFragment
import com.example.coreandroid.base.eventsSelectionActionModeController
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.PagedDataList
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.restoreScrollPosition
import com.example.coreandroid.util.ext.saveScrollPosition
import com.example.coreandroid.util.ext.snackbarController
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import com.example.coreandroid.view.epoxy.listItem
import kotlinx.android.synthetic.main.fragment_nearby.*
import kotlinx.android.synthetic.main.fragment_nearby.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class NearbyFragment : InjectableEpoxyFragment() {

    @Inject
    internal lateinit var fragmentProvider: IFragmentProvider

    @Inject
    internal lateinit var viewModel: NearbyViewModel

    private val eventsScrollListener: EndlessRecyclerViewScrollListener by lazy(LazyThreadSafetyMode.NONE) {
        EndlessRecyclerViewScrollListener {
            fragmentScope.launch { viewModel.send(EventListScrolledToEnd) }
        }
    }

    private val epoxyController by lazy(LazyThreadSafetyMode.NONE) {
        itemListController<PagedDataList<Selectable<Event>>, Selectable<Event>>(
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

    private val actionModeController: EventsSelectionActionModeController by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        eventsSelectionActionModeController(
            menuId = R.menu.nearby_events_selection_menu,
            itemClickedCallbacks = mapOf(
                R.id.nearby_action_add_favourite to {
                    fragmentScope.launch { viewModel.send(AddToFavouritesClicked) }.let { Unit }
                }
            ),
            onDestroyActionMode = {
                fragmentScope.launch { viewModel.send(ClearSelectionClicked) }.let { Unit }
            }
        )
    }

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

        viewModel.states.onEach { epoxyController.setData(it.events) }.launchIn(fragmentScope)

        viewModel.states
            .map { state -> state.events.value.count { it.selected } }
            .distinctUntilChanged()
            .onEach { actionModeController.update(it) }
            .launchIn(fragmentScope)

        viewModel.states
            .map { it.snackbarState }
            .distinctUntilChanged()
            .onEach { snackbarController?.transitionToSnackbarState(it) }
            .launchIn(fragmentScope)

        viewModel.events.observe(this, Observer {
            if (it is NearbySignal.FavouritesSaved) actionModeController.finish(true)
        })
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
        with(viewModel.state) {
            snackbarController?.transitionToSnackbarState(snackbarState)
            actionModeController.update(events.value.count { it.selected })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuController?.clearMenu()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        nearby_events_recycler_view?.saveScrollPosition(outState)
    }
}
