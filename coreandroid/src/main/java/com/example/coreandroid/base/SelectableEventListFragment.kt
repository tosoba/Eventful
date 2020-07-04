package com.example.coreandroid.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.TypedEpoxyController
import com.example.core.util.HoldsList
import com.example.coreandroid.controller.eventsSelectionActionModeController
import com.example.coreandroid.model.event.Event
import com.example.coreandroid.model.event.Selectable
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.provider.PopBackStackSignalProvider
import com.example.coreandroid.util.delegate.viewBinding
import com.example.coreandroid.util.ext.navigationFragment
import com.example.coreandroid.util.ext.saveScrollPosition
import com.example.coreandroid.util.ext.setControllerWithSavedState
import com.example.coreandroid.view.epoxy.EpoxyThreads
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
abstract class SelectableEventListFragment<
        VB : ViewBinding, D : Any, I : Any, S : SelectableEventsState<S>, VM : FlowViewModel<I, *, S, *>, VU>(
    @LayoutRes private val layoutRes: Int,
    viewBindingFactory: (View) -> VB,
    private val epoxyRecyclerView: VB.() -> EpoxyRecyclerView,
    private val mapToHoldsList: D.() -> HoldsList<Selectable<Event>>,
    private val emptyTextResource: ((D) -> Int)? = null,
    @MenuRes private val eventsSelectionMenuRes: Int,
    private val selectionConfirmedActionId: Int,
    private val loadMoreResultsIntent: I,
    private val selectionConfirmedIntent: I,
    private val clearSelectionIntent: I,
    private val eventSelectedIntent: (Event) -> I,
    private val viewUpdates: (VM).() -> Flow<VU>
) : DaggerViewModelFragment<VM>(layoutRes) {

    @Inject
    internal lateinit var fragmentFactory: IFragmentFactory

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

    @Inject
    internal lateinit var popBackStackSignalProvider: PopBackStackSignalProvider

    protected val binding: VB by viewBinding(viewBindingFactory)

    protected val epoxyController: TypedEpoxyController<D> by lazy(LazyThreadSafetyMode.NONE) {
        infiniteItemListController(
            epoxyThreads = epoxyThreads,
            mapToHoldsList = mapToHoldsList,
            emptyTextResource = emptyTextResource,
            loadMore = { lifecycleScope.launch { viewModel.intent(loadMoreResultsIntent) } }
        ) { selectable ->
            selectable.listItem(
                clicked = View.OnClickListener {
                    actionModeController.finish(false)
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
                lifecycleScope.launch { viewModel.intent(clearSelectionIntent) }.let { Unit }
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
    private var popBackStackSignalProviderJob: Job? = null

    @CallSuper
    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
        viewUpdatesJob = viewModel.viewUpdates().onEach(::onViewUpdate).launchIn(lifecycleScope)
        popBackStackSignalProviderJob = popBackStackSignalProvider.popBackStackSignals
            .onEach {
                actionModeController.update(viewModel.state.events.data.count { it.selected })
            }
            .launchIn(lifecycleScope)
    }

    @CallSuper
    override fun onPause() {
        viewUpdatesJob?.cancel()
        popBackStackSignalProviderJob?.cancel()
        super.onPause()
    }

    protected abstract suspend fun onViewUpdate(viewUpdate: VU)
}
