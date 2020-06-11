package com.example.nearby

import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.controller.eventsSelectionActionModeController
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.util.EpoxyThreads
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.util.infiniteItemListController
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
class NearbyFragment : DaggerViewModelFragment<NearbyViewModel>() {

    @Inject
    internal lateinit var fragmentFactory: IFragmentFactory

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

    private val epoxyController by lazy(LazyThreadSafetyMode.NONE) {
        infiniteItemListController<Selectable<Event>>(
            epoxyThreads,
            emptyText = "No events found",
            loadMore = { lifecycleScope.launch { viewModel.intent(NearbyIntent.LoadMoreResults) } }
        ) { selectable ->
            selectable.listItem(
                clicked = View.OnClickListener {
                    navigationFragment?.showFragment(fragmentFactory.eventFragment(selectable.item))
                },
                longClicked = View.OnLongClickListener {
                    lifecycleScope.launch {
                        viewModel.intent(NearbyIntent.EventLongClicked(selectable.item))
                    }
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
                    lifecycleScope.launch { viewModel.intent(NearbyIntent.AddToFavouritesClicked) }
                    Unit
                }
            ),
            onDestroyActionMode = {
                lifecycleScope.launch { viewModel.intent(NearbyIntent.ClearSelectionClicked) }
                Unit
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nearby, container, false).apply {
        this.nearby_events_recycler_view.onCreateControllerView(epoxyController, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        activity?.invalidateOptionsMenu()

        viewModel.states
            .map { it.events }
            .distinctUntilChanged()
            .onEach { epoxyController.setData(it) }
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

        viewModel.signals
            .onEach {
                if (it is NearbySignal.FavouritesSaved) actionModeController.finish(false)
            }
            .launchIn(lifecycleScope)
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
