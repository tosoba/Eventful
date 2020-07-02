package com.example.coreandroid.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.airbnb.epoxy.EpoxyRecyclerView
import com.example.coreandroid.controller.eventsSelectionActionModeController
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.view.epoxy.EpoxyThreads
import com.example.coreandroid.util.delegate.viewBinding
import com.example.coreandroid.util.ext.navigationFragment
import com.example.coreandroid.util.ext.saveScrollPosition
import com.example.coreandroid.util.ext.setControllerWithSavedState
import com.example.coreandroid.view.epoxy.infiniteItemListController
import com.example.coreandroid.view.epoxy.listItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
abstract class SelectableEventListFragment<Binding : ViewBinding, Intent : Any, VM : FlowViewModel<Intent, *, *, *>, ViewUpdate>(
    @LayoutRes private val layoutRes: Int,
    viewBindingFactory: (View) -> Binding,
    private val epoxyRecyclerView: Binding.() -> EpoxyRecyclerView,
    @MenuRes private val eventsSelectionMenuRes: Int,
    @StringRes private val emptyListTextRes: Int?,
    private val selectionConfirmedActionId: Int,
    private val loadMoreResultsIntent: Intent,
    private val selectionConfirmedIntent: Intent,
    private val clearSelectionIntent: Intent,
    private val eventSelectedIntent: (Event) -> Intent,
    private val viewUpdates: (VM).() -> Flow<ViewUpdate>
) : DaggerViewModelFragment<VM>(layoutRes) {

    @Inject
    internal lateinit var fragmentFactory: IFragmentFactory

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

    protected val binding: Binding by viewBinding(viewBindingFactory)

    protected val epoxyController by lazy(LazyThreadSafetyMode.NONE) {
        infiniteItemListController<Selectable<Event>>(
            epoxyThreads,
            emptyText = emptyListTextRes?.let { context?.getString(it) },
            loadMore = { lifecycleScope.launch { viewModel.intent(loadMoreResultsIntent) } }
        ) { selectable ->
            selectable.listItem(
                clicked = View.OnClickListener {
                    navigationFragment?.showFragment(fragmentFactory.eventFragment(selectable.item))
                },
                longClicked = View.OnLongClickListener {
                    lifecycleScope.launch {
                        viewModel.intent(eventSelectedIntent(selectable.item))
                    }
                    true
                }
            )
        }
    }

    protected val actionModeController by lazy(LazyThreadSafetyMode.NONE) {
        eventsSelectionActionModeController(
            menuId = eventsSelectionMenuRes,
            itemClickedCallbacks = mapOf(
                selectionConfirmedActionId to {
                    lifecycleScope.launch { viewModel.intent(selectionConfirmedIntent) }
                    Unit
                }
            ),
            onDestroyActionMode = {
                lifecycleScope.launch { viewModel.intent(clearSelectionIntent) }
                Unit
            }
        )
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.epoxyRecyclerView().setControllerWithSavedState(epoxyController, savedInstanceState)
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        binding.epoxyRecyclerView().saveScrollPosition(outState)
    }

    private var viewUpdatesJob: Job? = null

    @CallSuper
    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
        viewUpdatesJob = viewModel.viewUpdates().onEach(::onViewUpdate).launchIn(lifecycleScope)
    }

    @CallSuper
    override fun onPause() {
        viewUpdatesJob?.cancel()
        super.onPause()
    }

    protected abstract suspend fun onViewUpdate(viewUpdate: ViewUpdate)
}
