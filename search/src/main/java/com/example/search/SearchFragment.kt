package com.example.search

import android.app.SearchManager
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import com.example.coreandroid.base.InjectableVectorFragment
import com.example.coreandroid.di.Dependencies
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.util.SnackbarState
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import com.example.coreandroid.view.epoxy.listItem
import com.github.satoshun.coroutinebinding.androidx.appcompat.widget.queryTextChange
import com.github.satoshun.coroutinebinding.androidx.appcompat.widget.queryTextChangeEvents
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


class SearchFragment : InjectableVectorFragment() {

    @Inject
    internal lateinit var fragmentProvider: IFragmentProvider

    @Inject
    internal lateinit var handler: SearchViewEventHandler

    @Inject
    @field:Named(Dependencies.EPOXY_DIFFER)
    internal lateinit var differ: Handler

    @Inject
    @field:Named(Dependencies.EPOXY_BUILDER)
    internal lateinit var builder: Handler

    private val eventsScrollListener: EndlessRecyclerViewScrollListener by lazy {
        EndlessRecyclerViewScrollListener {
            handler.eventOccurred(Interaction.EventListScrolledToEnd)
        }
    }

    private val epoxyController by lazy {
        itemListController(
            builder, differ, handler.viewModel, SearchState::events,
            onScrollListener = eventsScrollListener,
            emptyText = "No events found"
        ) { event ->
            event.listItem(View.OnClickListener {
                handler.eventOccurred(Interaction.EventClicked(event))
            })
        }
    }

    private val searchSuggestionsAdapter: SearchSuggestionsAdapter by lazy {
        SearchSuggestionsAdapter(requireContext(), null)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenuIfVisible()

        fragmentScope.launch {
            handler.updates.collect {
                when (it) {
                    is InvalidateList -> {
                        if (it.hideSnackbar) snackbarController?.transitionTo(
                            SnackbarState.Hidden,
                            this@SearchFragment
                        )
                        epoxyController.setData(handler.viewModel.currentState)
                    }
                    is ShowEvent -> {
                        navigationFragment?.showFragment(fragmentProvider.eventFragment(it.event))
                    }
                    is ShowSnackbarAndInvalidateList -> {
                        snackbarController?.transitionTo(
                            SnackbarState.Text(it.msg),
                            this@SearchFragment
                        )
                        epoxyController.setData(handler.viewModel.currentState)
                        if (it.errorOccurred) eventsScrollListener.onLoadingError()
                    }
                    is UpdateSearchSuggestions -> searchSuggestionsAdapter.swapCursor(it.cursor)
                }
            }
        }
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

    override fun onDestroy() {
        handler.eventOccurred(Lifecycle.OnDestroy)
        super.onDestroy()
    }

    private fun initializeSearchView(searchView: SearchView) {
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setSearchableInfo(
            getSystemService<SearchManager>(
                requireContext(), SearchManager::class.java
            )?.getSearchableInfo(activity?.componentName)
        )
        searchView.suggestionsAdapter = searchSuggestionsAdapter

        fragmentScope.launch {
            searchView.queryTextChangeEvents()
                .consumeAsFlow()
                .filter { it.isSubmitted && it.queryText.isNotBlank() && it.queryText.length > 2 }
                .collect {
                    handler.eventOccurred(Interaction.SearchTextChanged(it.toString()))
                }
        }

        fragmentScope.launch {
            searchView.queryTextChange()
                .consumeAsFlow()
                .filter { it.isNotBlank() && it.length > 2 }
                .debounce(500)
                .distinctUntilChanged()
                .collect {
                    handler.eventOccurred(Interaction.SearchTextChanged(it.toString()))
                }
        }
    }
}
