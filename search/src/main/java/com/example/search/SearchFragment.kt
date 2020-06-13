package com.example.search

import android.app.SearchManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.SelectableEventListFragment
import com.example.coreandroid.util.ext.*
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
class SearchFragment : SelectableEventListFragment<SearchIntent, SearchViewModel>(
    layoutRes = R.layout.fragment_search,
    menuRes = R.menu.search_events_selection_menu,
    emptyListTextRes = R.string.no_events_found,
    selectionConfirmedActionId = R.id.search_action_add_favourite,
    loadMoreResultsIntent = SearchIntent.LoadMoreResults,
    selectionConfirmedIntent = SearchIntent.AddToFavouritesClicked,
    clearSelectionIntent = SearchIntent.ClearSelectionClicked,
    eventSelectedIntent = { SearchIntent.EventLongClicked(it) }
) {
    private val searchSuggestionsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        SearchSuggestionsAdapter(requireContext(), null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = super.onCreateView(inflater, container, savedInstanceState)?.apply {
        this.search_events_recycler_view.onCreateControllerView(epoxyController, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()

        viewModel.viewUpdates
            .onEach {
                when (it) {
                    is SearchViewUpdate.Events -> epoxyController.setData(it.events)
                    is SearchViewUpdate.Snackbar -> snackbarController?.transitionToSnackbarState(it.state)
                    is SearchViewUpdate.UpdateActionMode -> actionModeController.update(it.numberOfSelectedEvents)
                    is SearchViewUpdate.SwapCursor -> searchSuggestionsAdapter.swapCursor(it.cursor)
                    is SearchViewUpdate.FinishActionMode -> actionModeController.finish(false)
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuController?.initializeMenu(R.menu.search_menu, inflater) {
            (it.findItem(R.id.search_action)?.actionView as? SearchView)?.run(::initialize)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        search_events_recycler_view?.saveScrollPosition(outState)
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
