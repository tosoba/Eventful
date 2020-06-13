package com.example.search

import android.app.SearchManager
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.SelectableEventListFragment
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.snackbarController
import com.example.search.databinding.FragmentSearchBinding
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
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
    SelectableEventListFragment<FragmentSearchBinding, SearchIntent, SearchViewModel, SearchViewUpdate>(
        layoutRes = R.layout.fragment_search,
        viewBindingFactory = FragmentSearchBinding::bind,
        epoxyRecyclerView = FragmentSearchBinding::searchEventsRecyclerView,
        menuRes = R.menu.search_events_selection_menu,
        emptyListTextRes = R.string.no_events_found,
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
            .onEach {
                viewModel.intent(
                    SearchIntent.NewSearch(
                        text = it.queryText.toString().trim(),
                        confirmed = it is QueryTextEvent.QuerySubmitted
                    )
                )
            }
            .launchIn(lifecycleScope)
    }
}
