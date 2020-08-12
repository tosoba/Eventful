package com.eventful.core.android.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.TypedEpoxyController
import com.eventful.core.android.controller.ItemsSelectionActionModeController
import com.eventful.core.android.controller.itemsSelectionActionModeController
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.navigation.IMainChildFragmentNavDestinations
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.core.android.util.ext.*
import com.eventful.core.android.view.epoxy.EpoxyThreads
import com.eventful.core.android.view.epoxy.infiniteItemListController
import com.eventful.core.android.view.epoxy.listItem
import com.eventful.core.model.Selectable
import com.eventful.core.util.HoldsList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
abstract class SelectableEventListFragment<
        VB : ViewBinding, D : Any, I : Any, S : SelectableItemsState<S, Event>, VM : FlowViewModel<I, *, S, *>, VU>(
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
    internal lateinit var navDestinations: IMainChildFragmentNavDestinations

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

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
                    navigationFragment?.showFragment(navDestinations.eventFragment(selectable.item))
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

    protected val actionModeController: ItemsSelectionActionModeController by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        itemsSelectionActionModeController(
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
    private var backStackSignalsJob: Job? = null

    @CallSuper
    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()

        viewUpdatesJob = viewModel.viewUpdates()
            .onEachLogging(
                "VIEW_UPDATE",
                javaClass.simpleName.replace(LogType.FRAGMENT.name, ""),
                ::onViewUpdate
            )
            .launchIn(lifecycleScope)

        backStackSignalsJob = requireNotNull(navigationFragment).backStackSignals
            .filter { it }
            .onEach {
                actionModeController.update(viewModel.state.items.data.count { it.selected })
            }
            .launchIn(lifecycleScope)
    }

    @CallSuper
    override fun onPause() {
        viewUpdatesJob?.cancel()
        backStackSignalsJob?.cancel()
        super.onPause()
    }

    protected abstract suspend fun onViewUpdate(viewUpdate: VU)
}
