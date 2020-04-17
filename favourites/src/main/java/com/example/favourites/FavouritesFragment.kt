package com.example.favourites

import android.os.Bundle
import android.view.*
import com.example.coreandroid.base.InjectableEpoxyFragment
import com.example.coreandroid.base.clearMenu
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.util.ext.menuController
import com.example.coreandroid.util.ext.navigationFragment
import com.example.coreandroid.util.ext.restoreScrollPosition
import com.example.coreandroid.util.ext.saveScrollPosition
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import com.example.coreandroid.view.epoxy.listItem
import kotlinx.android.synthetic.main.fragment_favourites.*
import kotlinx.android.synthetic.main.fragment_favourites.view.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


class FavouritesFragment : InjectableEpoxyFragment() {

    @Inject
    internal lateinit var fragmentProvider: IFragmentProvider

    @Inject
    internal lateinit var viewModel: FavouritesVM

    private val eventsScrollListener: EndlessRecyclerViewScrollListener by lazy {
        EndlessRecyclerViewScrollListener(loadMore = {
            fragmentScope.launch { viewModel.send(LoadFavourites) }
        })
    }

    private val epoxyController by lazy {
        itemListController(
            viewModel, FavouritesState::events,
            emptyText = "No favourite events added yet",
            onScrollListener = eventsScrollListener
        ) { event ->
            event.listItem(View.OnClickListener {
                navigationFragment?.showFragment(fragmentProvider.eventFragment(event))
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_favourites, container, false).apply {
        this.favourite_events_recycler_view.setController(epoxyController)
        savedInstanceState?.let {
            this.favourite_events_recycler_view.restoreScrollPosition(
                savedInstanceState, epoxyController
            )
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.states.onEach { epoxyController.setData(it) }.launchIn(fragmentScope)
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuController?.clearMenu()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        favourite_events_recycler_view?.saveScrollPosition(outState)
    }
}
