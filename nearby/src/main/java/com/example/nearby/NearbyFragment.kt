package com.example.nearby

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.InjectableFragment
import com.example.coreandroid.controller.eventsSelectionActionModeController
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.EpoxyThreads
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.InfiniteRecyclerViewScrollListener
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
class NearbyFragment : InjectableFragment() {

    @Inject
    internal lateinit var fragmentFactory: IFragmentFactory

    @Inject
    internal lateinit var viewModel: NearbyViewModel

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

    private val eventsScrollListener by lazy(LazyThreadSafetyMode.NONE) {
        InfiniteRecyclerViewScrollListener {
            lifecycleScope.launch { viewModel.send(EventListScrolledToEnd) }
        }
    }

    private val epoxyController by lazy(LazyThreadSafetyMode.NONE) {
        itemListController<Selectable<Event>>(
            epoxyThreads,
            onScrollListener = eventsScrollListener
        ) { selectable ->
            selectable.listItem(
                clicked = View.OnClickListener {
                    navigationFragment?.showFragment(fragmentFactory.eventFragment(selectable.item))
                },
                longClicked = View.OnLongClickListener {
                    lifecycleScope.launch { viewModel.send(EventLongClicked(selectable.item)) }
                    true
                }
            )
        }
    }

    private val actionModeController by lazy(LazyThreadSafetyMode.NONE) {
        eventsSelectionActionModeController(
            menuId = R.menu.nearby_events_selection_menu,
            itemClickedCallbacks = mapOf(
                R.id.nearby_action_add_favourite to {
                    lifecycleScope.launch { viewModel.send(AddToFavouritesClicked) }.let { Unit }
                }
            ),
            onDestroyActionMode = {
                lifecycleScope.launch { viewModel.send(ClearSelectionClicked) }.let { Unit }
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
        this.nearby_events_recycler_view.onCreateControllerView(epoxyController, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        activity?.invalidateOptionsMenu()

        viewModel.states
            .map { it.events }
            .distinctUntilChanged()
            .onEach {
                epoxyController.setData(it)
                if (it.loadingFailed) eventsScrollListener.onLoadingError()
            }
            .launchIn(lifecycleScope)

        viewModel.states
            .map { state -> state.events.data.count { it.selected } }
            .distinctUntilChanged()
            .onEach { actionModeController.update(it) }
            .launchIn(lifecycleScope)

        viewModel.states
            .map { it.snackbarState }
            .distinctUntilChanged()
            .onEach { snackbarController?.transitionToSnackbarState(it) }
            .launchIn(lifecycleScope)

        viewModel.events.observe(this, Observer {
            if (it is NearbySignal.FavouritesSaved) actionModeController.finish(false)
        })
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
