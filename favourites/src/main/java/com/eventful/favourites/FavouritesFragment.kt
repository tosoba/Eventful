package com.eventful.favourites

import android.view.Menu
import android.view.MenuInflater
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.eventful.core.android.base.SelectableEventListFragment
import com.eventful.core.android.util.ext.menuController
import com.eventful.core.android.util.ext.snackbarController
import com.eventful.favourites.databinding.FragmentFavouritesBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import reactivecircus.flowbinding.appcompat.queryTextEvents

@ExperimentalCoroutinesApi
@FlowPreview
class FavouritesFragment :
    SelectableEventListFragment<FragmentFavouritesBinding, FavouriteEventsData, FavouritesIntent, FavouritesState, FavouritesViewModel, FavouritesViewUpdate>(
        layoutRes = R.layout.fragment_favourites,
        viewBindingFactory = FragmentFavouritesBinding::bind,
        epoxyRecyclerView = FragmentFavouritesBinding::favouriteEventsRecyclerView,
        mapToHoldsList = { events },
        emptyTextResource = { state ->
            if (state.searchText.isNotBlank()) R.string.no_favourite_events_match_search_text
            else R.string.no_favourite_events_added_yet
        },
        eventsSelectionMenuRes = R.menu.favourites_events_selection_menu,
        selectionConfirmedActionId = R.id.favourites_action_remove_favourite,
        loadMoreResultsIntent = FavouritesIntent.LoadFavourites,
        selectionConfirmedIntent = FavouritesIntent.RemoveFromFavouritesClicked,
        clearSelectionIntent = FavouritesIntent.ClearSelectionClicked,
        eventSelectedIntent = { FavouritesIntent.EventLongClicked(it) },
        viewUpdates = FavouritesViewModel::viewUpdates
    ) {

    override suspend fun onViewUpdate(viewUpdate: FavouritesViewUpdate) {
        when (viewUpdate) {
            is FavouritesViewUpdate.Events -> epoxyController.setData(viewUpdate.eventsData)
            is FavouritesViewUpdate.Snackbar -> snackbarController?.transitionToSnackbarState(
                viewUpdate.state
            )
            is FavouritesViewUpdate.UpdateActionMode -> actionModeController.update(
                viewUpdate.numberOfSelectedEvents
            )
            is FavouritesViewUpdate.FinishActionMode -> actionModeController.finish(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuController?.initializeMenu(R.menu.favourites_menu, inflater) {
            (it.findItem(R.id.favourites_search_action)?.actionView as? SearchView)?.run(::initialize)
        }
    }

    private fun initialize(searchView: SearchView) = searchView.apply {
        maxWidth = Integer.MAX_VALUE
        queryTextEvents()
            .debounce(500)
            .map { it.queryText.toString().trim() }
            .distinctUntilChanged()
            .onEach { viewModel.intent(FavouritesIntent.NewSearch(it)) }
            .launchIn(lifecycleScope)
    }
}
