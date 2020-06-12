package com.example.nearby

import android.os.Bundle
import android.view.*
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.SelectableEventListFragment
import com.example.coreandroid.navigation.EventFragmentClassProvider
import com.example.coreandroid.util.EpoxyThreads
import com.example.coreandroid.util.ext.*
import kotlinx.android.synthetic.main.fragment_nearby.*
import kotlinx.android.synthetic.main.fragment_nearby.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Provider

@FlowPreview
@ExperimentalCoroutinesApi
class NearbyFragment @Inject constructor(
    viewModelProvider: Provider<NearbyViewModel>,
    epoxyThreads: EpoxyThreads,
    fragmentClassProvider: EventFragmentClassProvider
) : SelectableEventListFragment<NearbyIntent, NearbyViewModel>(
    viewModelProvider = viewModelProvider,
    layoutRes = R.layout.fragment_nearby,
    menuRes = R.menu.nearby_events_selection_menu,
    selectionConfirmedActionId = R.id.nearby_action_add_favourite,
    emptyListTextRes = R.string.no_events_found,
    loadMoreResultsIntent = NearbyIntent.LoadMoreResults,
    selectionConfirmedIntent = NearbyIntent.AddToFavouritesClicked,
    clearSelectionIntent = NearbyIntent.ClearSelectionClicked,
    eventSelectedIntent = { NearbyIntent.EventLongClicked(it) },
    epoxyThreads = epoxyThreads,
    fragmentClassProvider = fragmentClassProvider
) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = super.onCreateView(inflater, container, savedInstanceState)?.apply {
        this.nearby_events_recycler_view.onCreateControllerView(epoxyController, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()

        viewModel.viewUpdates
            .onEach {
                when (it) {
                    is NearbyViewUpdate.Events -> epoxyController.setData(it.events)
                    is NearbyViewUpdate.Snackbar -> snackbarController?.transitionToSnackbarState(it.state)
                    is NearbyViewUpdate.UpdateActionMode -> actionModeController.update(it.numberOfSelectedEvents)
                    is NearbyViewUpdate.FinishActionMode -> actionModeController.finish(false)
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
        nearby_events_recycler_view?.saveScrollPosition(outState)
    }
}
