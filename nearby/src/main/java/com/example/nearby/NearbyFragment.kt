package com.example.nearby

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.core.util.HoldsList
import com.example.coreandroid.base.SelectableEventListFragment
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.snackbarController
import com.example.nearby.databinding.FragmentNearbyBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class NearbyFragment :
    SelectableEventListFragment<FragmentNearbyBinding, HoldsList<Selectable<Event>>, NearbyIntent, NearbyState, NearbyViewModel, NearbyViewUpdate>(
        layoutRes = R.layout.fragment_nearby,
        viewBindingFactory = FragmentNearbyBinding::bind,
        epoxyRecyclerView = FragmentNearbyBinding::nearbyEventsRecyclerView,
        mapToHoldsList = { this },
        emptyTextResource = { R.string.no_events_found },
        eventsSelectionMenuRes = R.menu.nearby_events_selection_menu,
        selectionConfirmedActionId = R.id.nearby_action_add_favourite,
        loadMoreResultsIntent = NearbyIntent.LoadMoreResults,
        selectionConfirmedIntent = NearbyIntent.AddToFavouritesClicked,
        clearSelectionIntent = NearbyIntent.ClearSelectionClicked,
        eventSelectedIntent = { NearbyIntent.EventLongClicked(it) },
        viewUpdates = NearbyViewModel::viewUpdates
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nearbySwipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch { viewModel.intent(NearbyIntent.ReloadLocation) }
        }
    }

    override suspend fun onViewUpdate(viewUpdate: NearbyViewUpdate) {
        when (viewUpdate) {
            is NearbyViewUpdate.Events -> {
                epoxyController.setData(viewUpdate.events)
                binding.nearbySwipeRefreshLayout.isEnabled = viewUpdate.events.data.isNotEmpty()
            }
            is NearbyViewUpdate.Snackbar -> snackbarController?.transitionToSnackbarState(
                viewUpdate.state
            )
            is NearbyViewUpdate.UpdateActionMode -> actionModeController.update(
                viewUpdate.numberOfSelectedEvents
            )
            is NearbyViewUpdate.FinishActionMode -> actionModeController.finish(false)
            is NearbyViewUpdate.StopRefreshingIfInProgress -> {
                binding.nearbySwipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuController?.clearMenu()
    }
}
