package com.example.search

import android.app.SearchManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.InjectableFragment
import com.example.coreandroid.controller.eventsSelectionActionModeController
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.ticketmaster.Event
import com.example.coreandroid.ticketmaster.Selectable
import com.example.coreandroid.util.EpoxyThreads
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.epoxy.listItem
import com.example.coreandroid.view.infiniteRecyclerViewScrollListener
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.appcompat.QueryTextEvent
import reactivecircus.flowbinding.appcompat.queryTextEvents
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class SearchFragment : InjectableFragment() {

    @Inject
    internal lateinit var fragmentFactory: IFragmentFactory

    @Inject
    internal lateinit var viewModel: SearchViewModel

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

    private val eventsScrollListener by lazy(LazyThreadSafetyMode.NONE) {
        infiniteRecyclerViewScrollListener { viewModel.send(LoadMoreResults) }
    }

    private val epoxyController by lazy(LazyThreadSafetyMode.NONE) {
        itemListController<Selectable<Event>>(
            epoxyThreads,
            onScrollListener = eventsScrollListener,
            emptyText = "No events found"
        ) { selectable ->
            selectable.listItem(
                clicked = View.OnClickListener {
                    navigationFragment?.showFragment(fragmentFactory.eventFragment(selectable.item))
                },
                longClicked = View.OnLongClickListener {
                    lifecycleScope.launch { viewModel.send(EventLongClicked(selectable.item)) }
                    true
                }
            )
        }
    }

    private val actionModeController by lazy(LazyThreadSafetyMode.NONE) {
        eventsSelectionActionModeController(
            menuId = R.menu.search_events_selection_menu,
            itemClickedCallbacks = mapOf(
                R.id.search_action_add_favourite to {
                    lifecycleScope.launch { viewModel.send(AddToFavouritesClicked) }.let { Unit }
                }
            ),
            onDestroyActionMode = {
                lifecycleScope.launch { viewModel.send(ClearSelectionClicked) }.let { Unit }
            }
        )
    }

    private val searchSuggestionsAdapter by lazy(LazyThreadSafetyMode.NONE) {
        SearchSuggestionsAdapter(requireContext(), null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false).apply {
        this.search_events_recycler_view.onCreateControllerView(epoxyController, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()

        viewModel.updates().onEach {
            when (it) {
                is UpdateEvents -> epoxyController.setData(it.events)
                is UpdateSnackbar -> snackbarController?.transitionToSnackbarState(it.state)
                is SwapCursor -> searchSuggestionsAdapter.swapCursor(it.cursor)
                is UpdateActionMode -> actionModeController.update(it.numberOfSelectedEvents)
            }
        }.launchIn(lifecycleScope)

        viewModel.signals.observe(this, Observer {
            if (it is SearchSignal.FavouritesSaved) actionModeController.finish(false)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuController?.initializeMenu(R.menu.search_menu, inflater) {
            (it.findItem(R.id.search_action)?.actionView as? SearchView)
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
            getSystemService(requireContext(), SearchManager::class.java)?.getSearchableInfo(
                activity?.componentName
            )
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
            .launchIn(lifecycleScope)
    }
}
