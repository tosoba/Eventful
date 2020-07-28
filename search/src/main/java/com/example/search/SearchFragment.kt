package com.example.search

import android.app.SearchManager
import android.view.Menu
import android.view.MenuInflater
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import com.example.core.util.HoldsList
import com.example.coreandroid.base.SelectableEventListFragment
import com.example.coreandroid.model.event.Event
import com.example.core.model.Selectable
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.snackbarController
import com.example.search.databinding.FragmentSearchBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.appcompat.QueryTextEvent
import reactivecircus.flowbinding.appcompat.queryTextEvents

@ExperimentalCoroutinesApi
@FlowPreview
class SearchFragment :
    SelectableEventListFragment<FragmentSearchBinding, HoldsList<Selectable<Event>>, SearchIntent, SearchState, SearchViewModel, SearchViewUpdate>(
        layoutRes = R.layout.fragment_search,
        viewBindingFactory = FragmentSearchBinding::bind,
        epoxyRecyclerView = FragmentSearchBinding::searchEventsRecyclerView,
        mapToHoldsList = { this },
        emptyTextResource = { R.string.no_events_found },
        eventsSelectionMenuRes = R.menu.search_events_selection_menu,
        selectionConfirmedActionId = R.id.search_action_add_favourite,
        loadMoreResultsIntent = SearchIntent.LoadMoreResults,
        selectionConfirmedIntent = SearchIntent.AddToFavouritesClicked,
        clearSelectionIntent = SearchIntent.ClearSelectionClicked,
        eventSelectedIntent = { SearchIntent.EventLongClicked(it) },
        viewUpdates = SearchViewModel::viewUpdates
    ) {

    private val searchSuggestionsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        SearchSuggestionsAdapter(requireContext(), null)
    }

    override suspend fun onViewUpdate(viewUpdate: SearchViewUpdate) {
        when (viewUpdate) {
            is SearchViewUpdate.Events -> epoxyController.setData(viewUpdate.events)
            is SearchViewUpdate.Snackbar -> snackbarController?.transitionToSnackbarState(viewUpdate.state)
            is SearchViewUpdate.UpdateActionMode -> actionModeController.update(viewUpdate.numberOfSelectedEvents)
            is SearchViewUpdate.SwapCursor -> searchSuggestionsAdapter.swapCursor(viewUpdate.cursor)
            is SearchViewUpdate.FinishActionMode -> actionModeController.finish(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuController?.initializeMenu(R.menu.search_menu, inflater) {
            (it.findItem(R.id.search_action)?.actionView as? SearchView)?.run(::initialize)
        }
    }

    private fun initialize(searchView: SearchView) = searchView.apply {
        maxWidth = Integer.MAX_VALUE
        setSearchableInfo(
            getSystemService(requireContext(), SearchManager::class.java)
                ?.getSearchableInfo(activity?.componentName)
        )
        suggestionsAdapter = searchSuggestionsAdapter
        queryTextEvents()
            .debounce(500)
            .filter { it.queryText.isNotBlank() && it.queryText.length > 2 }
            .onEach { event ->
                viewModel.intent(
                    SearchIntent.NewSearch(
                        text = event.queryText.toString().trim(),
                        confirmed = event is QueryTextEvent.QuerySubmitted
                    )
                )
            }
            .launchIn(lifecycleScope)
    }
}
