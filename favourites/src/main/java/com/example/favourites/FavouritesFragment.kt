package com.example.favourites

import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.InjectableFragment
import com.example.coreandroid.controller.eventsSelectionActionModeController
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import com.example.coreandroid.util.EpoxyThreads
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.util.infiniteItemListController
import com.example.coreandroid.view.epoxy.listItem
import kotlinx.android.synthetic.main.fragment_favourites.*
import kotlinx.android.synthetic.main.fragment_favourites.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class FavouritesFragment : InjectableFragment() {

    @Inject
    internal lateinit var fragmentFactory: IFragmentFactory

    @Inject
    internal lateinit var viewModel: FavouritesViewModel

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

    private val epoxyController by lazy(LazyThreadSafetyMode.NONE) {
        infiniteItemListController<Selectable<Event>>(
            epoxyThreads,
            emptyText = "No favourite events added yet",
            loadMore = { lifecycleScope.launch { viewModel.intent(LoadFavourites) } }
        ) { selectable ->
            selectable.listItem(
                clicked = View.OnClickListener {
                    navigationFragment?.showFragment(fragmentFactory.eventFragment(selectable.item))
                },
                longClicked = View.OnLongClickListener {
                    lifecycleScope.launch { viewModel.intent(EventLongClicked(selectable.item)) }
                    true
                }
            )
        }
    }

    private val actionModeController by lazy(LazyThreadSafetyMode.NONE) {
        eventsSelectionActionModeController(
            menuId = R.menu.favourites_events_selection_menu,
            itemClickedCallbacks = mapOf(
                R.id.favourites_action_remove_favourite to {
                    lifecycleScope.launch { viewModel.intent(RemoveFromFavouritesClicked) }
                    Unit
                }
            ),
            onDestroyActionMode = {
                lifecycleScope.launch { viewModel.intent(ClearSelectionClicked) }.let { Unit }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_favourites, container, false).apply {
        this.favourite_events_recycler_view.onCreateControllerView(
            epoxyController, savedInstanceState
        )
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
                if (it is FavouritesSignal.FavouritesRemoved) {
                    actionModeController.finish(false)
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuController?.clearMenu()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        favourite_events_recycler_view?.saveScrollPosition(outState)
    }
}
