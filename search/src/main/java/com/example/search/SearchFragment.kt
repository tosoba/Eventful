package com.example.search

import android.app.SearchManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import com.example.coreandroid.base.InjectableEpoxyFragment
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.restoreScrollPosition
import com.example.coreandroid.util.ext.saveScrollPosition
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import com.example.coreandroid.view.epoxy.listItem
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.appcompat.QueryTextEvent
import reactivecircus.flowbinding.appcompat.queryTextEvents
import javax.inject.Inject


class SearchFragment : InjectableEpoxyFragment() {

    @Inject
    internal lateinit var fragmentProvider: IFragmentProvider

    @Inject
    internal lateinit var viewModel: SearchVM

    private val eventsScrollListener: EndlessRecyclerViewScrollListener by lazy {
        EndlessRecyclerViewScrollListener {
            fragmentScope.launch { viewModel.send(LoadMoreResults) }
        }
    }

    private val epoxyController by lazy {
        itemListController(
            viewModel, SearchState::events,
            onScrollListener = eventsScrollListener,
            emptyText = "No events found"
        ) { event ->
            event.listItem(View.OnClickListener {
                //TODO: navigate to EventFragment
            })
        }
    }

    private val searchSuggestionsAdapter: SearchSuggestionsAdapter by lazy {
        SearchSuggestionsAdapter(requireContext(), null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false).apply {
        this.search_events_recycler_view.setController(epoxyController)
        savedInstanceState?.let {
            this.search_events_recycler_view.restoreScrollPosition(
                savedInstanceState, epoxyController
            )
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
        //TODO: snackbars
        //TODO: cursor swap
        viewModel.states.onEach { epoxyController.setData(it) }.launchIn(fragmentScope)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuController?.menuView?.let { menuView ->
            menuView.menu.clear()
            inflater.inflate(R.menu.search_menu, menuView.menu)
            (menuView.menu.findItem(R.id.search_action)?.actionView as? SearchView)
                ?.let(::initializeSearchView)
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        search_events_recycler_view?.saveScrollPosition(outState)
    }

    private fun initializeSearchView(searchView: SearchView) {
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setSearchableInfo(
            getSystemService<SearchManager>(
                requireContext(), SearchManager::class.java
            )?.getSearchableInfo(activity?.componentName)
        )
        searchView.suggestionsAdapter = searchSuggestionsAdapter
        searchView.queryTextEvents()
            .debounce(500)
            .filter { it.queryText.isNotBlank() && it.queryText.length > 2 }
            .onEach {
                viewModel.send(
                    NewSearch(it.queryText.toString(), it is QueryTextEvent.QuerySubmitted)
                )
            }
            .launchIn(fragmentScope)
    }
}
