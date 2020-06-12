package com.example.coreandroid.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import com.example.coreandroid.controller.eventsSelectionActionModeController
import com.example.coreandroid.model.Event
import com.example.coreandroid.model.Selectable
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.util.EpoxyThreads
import com.example.coreandroid.util.ext.navigationFragment
import com.example.coreandroid.util.infiniteItemListController
import com.example.coreandroid.view.epoxy.listItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
open class SelectableEventListFragment<Intent : Any, VM : FlowViewModel<Intent, *, *, *>>(
    @LayoutRes private val layoutRes: Int,
    @MenuRes private val menuRes: Int,
    @StringRes private val emptyListTextRes: Int?,
    private val selectionConfirmedActionId: Int,
    private val loadMoreResultsIntent: Intent,
    private val selectionConfirmedIntent: Intent,
    private val clearSelectionIntent: Intent,
    private val eventSelectedIntent: (Event) -> Intent
) : DaggerViewModelFragment<VM>(layoutRes) {

    @Inject
    internal lateinit var fragmentFactory: IFragmentFactory

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

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
            menuId = menuRes,
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
}
