package com.example.favourites

import android.view.Menu
import android.view.MenuInflater
import com.example.coreandroid.base.SelectableEventListFragment
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.snackbarController
import com.example.favourites.databinding.FragmentFavouritesBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class FavouritesFragment :
    SelectableEventListFragment<FragmentFavouritesBinding, FavouritesIntent, FavouritesViewModel, FavouritesViewUpdate>(
        layoutRes = R.layout.fragment_favourites,
        viewBindingFactory = FragmentFavouritesBinding::bind,
        epoxyRecyclerView = FragmentFavouritesBinding::favouriteEventsRecyclerView,
        menuRes = R.menu.favourites_events_selection_menu,
        emptyListTextRes = R.string.no_favourite_events_added_yet,
        selectionConfirmedActionId = R.id.favourites_action_remove_favourite,
        loadMoreResultsIntent = FavouritesIntent.LoadFavourites,
        selectionConfirmedIntent = FavouritesIntent.RemoveFromFavouritesClicked,
        clearSelectionIntent = FavouritesIntent.ClearSelectionClicked,
        eventSelectedIntent = { FavouritesIntent.EventLongClicked(it) },
        viewUpdates = FavouritesViewModel::viewUpdates
    ) {

    override suspend fun onViewUpdate(viewUpdate: FavouritesViewUpdate) {
        when (viewUpdate) {
            is FavouritesViewUpdate.Events -> epoxyController.setData(viewUpdate.events)
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
        menuController?.clearMenu()
    }
}
