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
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
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
            onScrollListener = eventsScrollListener
        ) { event ->
            event.listItem(View.OnClickListener {
                handler.eventOccurred(Interaction.EventClicked(event))
            })
        }
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
                        if (it.hideSnackbar) snackbarController?.transition(SnackbarState.Hidden)
                        epoxyController.setData(handler.viewModel.currentState)
                    }
                    is ShowEvent -> {
                        navigationFragment?.showFragment(fragmentProvider.eventFragment(it.event))
                    }
                    is ShowSnackbarAndInvalidateList -> {
                        snackbarController?.transition(SnackbarState.Text(it.msg))
                        epoxyController.setData(handler.viewModel.currentState)
                        if (it.errorOccurred) eventsScrollListener.onLoadingError()
                    }
                }
            }
        }
    }

    //TODO: add content provider for suggestions
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuController?.menuView?.let { menuView ->
            menuView.menu.clear()
            inflater.inflate(R.menu.search_menu, menuView.menu)
            (menuView.menu.findItem(R.id.search_action)?.actionView as? SearchView)?.let { searchView ->
                searchView.maxWidth = Integer.MAX_VALUE
                val searchManager = getSystemService<SearchManager>(
                    requireContext(), SearchManager::class.java
                )
                searchView.setSearchableInfo(searchManager?.getSearchableInfo(activity?.componentName))
                fragmentScope.launch {
                    searchView.queryTextChange()
                        .consumeAsFlow()
                        .debounce(500)
                        .collect {
                            this@SearchFragment.handler.eventOccurred(
                                Interaction.SearchTextChanged(it.toString())
                            )
                        }
                }
            }
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
}
