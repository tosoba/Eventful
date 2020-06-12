package com.example.favourites

import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.SelectableEventListFragment
import com.example.coreandroid.navigation.EventFragmentClassProvider
import com.example.coreandroid.util.EpoxyThreads
import com.example.coreandroid.util.ext.*
import kotlinx.android.synthetic.main.fragment_favourites.*
import kotlinx.android.synthetic.main.fragment_favourites.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Provider

@ExperimentalCoroutinesApi
@FlowPreview
class FavouritesFragment @Inject constructor(
    viewModelProvider: Provider<FavouritesViewModel>,
    epoxyThreads: EpoxyThreads,
    fragmentClassProvider: EventFragmentClassProvider
) : SelectableEventListFragment<FavouritesIntent, FavouritesViewModel>(
    viewModelProvider = viewModelProvider,
    layoutRes = R.layout.fragment_favourites,
    menuRes = R.menu.favourites_events_selection_menu,
    selectionConfirmedActionId = R.id.favourites_action_remove_favourite,
    emptyListTextRes = R.string.no_favourite_events_added_yet,
    loadMoreResultsIntent = FavouritesIntent.LoadFavourites,
    selectionConfirmedIntent = FavouritesIntent.RemoveFromFavouritesClicked,
    clearSelectionIntent = FavouritesIntent.ClearSelectionClicked,
    eventSelectedIntent = { FavouritesIntent.EventLongClicked(it) },
    epoxyThreads = epoxyThreads,
    fragmentClassProvider = fragmentClassProvider
) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = super.onCreateView(inflater, container, savedInstanceState)?.apply {
        this.favourite_events_recycler_view.onCreateControllerView(
            epoxyController, savedInstanceState
        )
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()

        viewModel.viewUpdates
            .onEach {
                when (it) {
                    is FavouritesViewUpdate.Events -> epoxyController.setData(it.events)
                    is FavouritesViewUpdate.Snackbar -> snackbarController
                        ?.transitionToSnackbarState(it.state)
                    is FavouritesViewUpdate.UpdateActionMode -> actionModeController
                        .update(it.numberOfSelectedEvents)
                    is FavouritesViewUpdate.FinishActionMode -> actionModeController
                        .finish(false)
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
