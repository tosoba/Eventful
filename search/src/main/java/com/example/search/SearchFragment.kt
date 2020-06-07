package com.example.search

import android.app.SearchManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.base.InjectableFragment
import com.example.coreandroid.controller.eventsSelectionActionModeController
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import com.example.coreandroid.util.EpoxyThreads
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.util.infiniteItemListController
import com.example.coreandroid.view.epoxy.listItem
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

    private val epoxyController by lazy(LazyThreadSafetyMode.NONE) {
        infiniteItemListController<Selectable<Event>>(
            epoxyThreads,
            emptyText = "No events found",
            loadMore = { lifecycleScope.launch { viewModel.intent(SearchIntent.LoadMoreResults) } }
        ) { selectable ->
            selectable.listItem(
                clicked = View.OnClickListener {
                    navigationFragment?.showFragment(fragmentFactory.eventFragment(selectable.item))
                },
                longClicked = View.OnLongClickListener {
                    lifecycleScope.launch {
                        viewModel.intent(SearchIntent.EventLongClicked(selectable.item))
                    }
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
                    lifecycleScope.launch { viewModel.intent(SearchIntent.AddToFavouritesClicked) }
                    Unit
                }
            ),
            onDestroyActionMode = {
                lifecycleScope.launch { viewModel.intent(SearchIntent.ClearSelectionClicked) }
                Unit
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

        viewModel.viewUpdates
            .onEach {
                when (it) {
                    is SearchViewUpdate.Events -> epoxyController.setData(it.events)
                    is SearchViewUpdate.Snackbar -> snackbarController?.transitionToSnackbarState(it.state)
                    is SearchViewUpdate.ActionMode -> actionModeController.update(it.numberOfSelectedEvents)
                    is SearchViewUpdate.SwapCursor -> searchSuggestionsAdapter.swapCursor(it.cursor)
                }
            }
            .launchIn(lifecycleScope)

        viewModel.signals
            .onEach {
                if (it is SearchSignal.FavouritesSaved) actionModeController.finish(false)
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
