package com.example.nearby

import android.view.Menu
import android.view.MenuInflater
import com.example.coreandroid.base.SelectableEventListFragment
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.snackbarController
import com.example.nearby.databinding.FragmentNearbyBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class NearbyFragment :
    SelectableEventListFragment<FragmentNearbyBinding, NearbyIntent, NearbyViewModel, NearbyViewUpdate>(
        layoutRes = R.layout.fragment_nearby,
        viewBindingFactory = FragmentNearbyBinding::bind,
        epoxyRecyclerView = FragmentNearbyBinding::nearbyEventsRecyclerView,
        menuRes = R.menu.nearby_events_selection_menu,
        emptyListTextRes = R.string.no_events_found,
        selectionConfirmedActionId = R.id.nearby_action_add_favourite,
        loadMoreResultsIntent = NearbyIntent.LoadMoreResults,
        selectionConfirmedIntent = NearbyIntent.AddToFavouritesClicked,
        clearSelectionIntent = NearbyIntent.ClearSelectionClicked,
        eventSelectedIntent = { NearbyIntent.EventLongClicked(it) },
        viewUpdates = NearbyViewModel::viewUpdates
    ) {

    override suspend fun onViewUpdate(viewUpdate: NearbyViewUpdate) {
        when (viewUpdate) {
            is NearbyViewUpdate.Events -> epoxyController.setData(viewUpdate.events)
            is NearbyViewUpdate.Snackbar -> snackbarController?.transitionToSnackbarState(
                viewUpdate.state
            )
            is NearbyViewUpdate.UpdateActionMode -> actionModeController.update(
                viewUpdate.numberOfSelectedEvents
            )
            is NearbyViewUpdate.FinishActionMode -> actionModeController.finish(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuController?.clearMenu()
    }
}
