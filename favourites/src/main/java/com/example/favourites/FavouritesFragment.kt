package com.example.favourites

import android.os.Bundle
import android.os.Handler
import android.view.*
import com.example.coreandroid.base.InjectableVectorFragment
import com.example.coreandroid.di.Dependencies
import com.example.coreandroid.navigation.IFragmentProvider
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.util.itemListController
import com.example.coreandroid.view.EndlessRecyclerViewScrollListener
import com.example.coreandroid.view.epoxy.listItem
import kotlinx.android.synthetic.main.fragment_favourites.*
import kotlinx.android.synthetic.main.fragment_favourites.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


class FavouritesFragment : InjectableVectorFragment() {

    @Inject
    internal lateinit var fragmentProvider: IFragmentProvider

    @Inject
    @field:Named(Dependencies.EPOXY_DIFFER)
    internal lateinit var differ: Handler

    @Inject
    @field:Named(Dependencies.EPOXY_BUILDER)
    internal lateinit var builder: Handler

    @Inject
    internal lateinit var viewModel: FavouritesViewModel

    private val eventsScrollListener: EndlessRecyclerViewScrollListener by lazy {
        EndlessRecyclerViewScrollListener { viewModel.loadMoreEvents() }
    }

    private val epoxyController by lazy {
        itemListController(
            builder, differ, viewModel, FavouritesState::events,
            emptyText = "No favourite events added yet",
            onScrollListener = eventsScrollListener
        ) { event ->
            event.listItem(View.OnClickListener {
                navigationFragment?.showFragment(fragmentProvider.eventFragment(event))
            })
        }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenuIfVisible()
        fragmentScope.launch {
            viewModel.state.collect { epoxyController.setData(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menuController?.menuView?.menu?.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        favourite_events_recycler_view?.saveScrollPosition(outState)
    }
}
