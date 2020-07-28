package com.example.alarms

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.TypedEpoxyController
import com.example.alarms.databinding.FragmentAlarmsBinding
import com.example.core.model.Selectable
import com.example.core.util.HoldsList
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.base.HasArgs
import com.example.coreandroid.controller.ItemsSelectionActionModeController
import com.example.coreandroid.controller.itemsSelectionActionModeController
import com.example.coreandroid.model.alarm.Alarm
import com.example.coreandroid.navigation.IFragmentFactory
import com.example.coreandroid.util.delegate.FragmentArgument
import com.example.coreandroid.util.delegate.viewBinding
import com.example.coreandroid.util.ext.*
import com.example.coreandroid.view.epoxy.EpoxyThreads
import com.example.coreandroid.view.epoxy.infiniteItemListController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class AlarmsFragment : DaggerViewModelFragment<AlarmsViewModel>(R.layout.fragment_alarms), HasArgs {

    private var mode: AlarmsMode by FragmentArgument()
    override val args: Bundle get() = bundleOf(MODE_ARG_KEY to mode)

    private val binding: FragmentAlarmsBinding by viewBinding(FragmentAlarmsBinding::bind)

    @Inject
    internal lateinit var epoxyThreads: EpoxyThreads

    @Inject
    internal lateinit var fragmentFactory: IFragmentFactory

    private val epoxyController: TypedEpoxyController<HoldsList<Selectable<Alarm>>> by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        infiniteItemListController<HoldsList<Selectable<Alarm>>, Selectable<Alarm>>(
            epoxyThreads = epoxyThreads,
            mapToHoldsList = { this },
            emptyTextResource = { R.string.no_created_alarms },
            loadMore = { lifecycleScope.launch { viewModel.intent(AlarmsIntent.LoadAlarms) } }
        ) { selectable ->
            selectable.listItem(
                clicked = View.OnClickListener {
                    if (mode is AlarmsMode.All) {
                        actionModeController.finish(false)
                        navigationFragment?.showFragment(
                            fragmentFactory.eventFragment(selectable.item.event)
                        )
                    }
                },
                longClicked = View.OnLongClickListener {
                    lifecycleScope.launch {
                        viewModel.intent(AlarmsIntent.AlarmLongClicked(selectable.item))
                    }
                    true
                }
            )
        }
    }

    private val actionModeController: ItemsSelectionActionModeController by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        itemsSelectionActionModeController(
            menuId = R.menu.alarms_selection_menu,
            itemClickedCallbacks = mapOf(
                R.id.action_remove_alarms to {
                    lifecycleScope.launch { viewModel.intent(AlarmsIntent.RemoveAlarmsClicked) }
                    Unit
                }
            ),
            onDestroyActionMode = {
                lifecycleScope.launch { viewModel.intent(AlarmsIntent.ClearSelectionClicked) }
                Unit
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (mode is AlarmsMode.All) binding.alarmsFab.visibility = View.GONE
        binding.alarmsRecyclerView.setController(epoxyController)
    }

    private var viewUpdatesJob: Job? = null

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.alarmsToolbar)
        showBackNavArrow()
        activity?.statusBarColor = context?.themeColor(R.attr.colorPrimaryDark)

        viewModel.viewUpdates
            .onEach { update ->
                when (update) {
                    is AlarmsViewUpdate.Events -> epoxyController.setData(update.alarms)
                    is AlarmsViewUpdate.Snackbar -> snackbarController?.transitionToSnackbarState(
                        update.state
                    )
                    is AlarmsViewUpdate.UpdateActionMode -> actionModeController.update(
                        update.numberOfSelectedAlarms
                    )
                    is AlarmsViewUpdate.FinishActionMode -> actionModeController.finish(false)
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onPause() {
        viewUpdatesJob?.cancel()
        super.onPause()
    }

    companion object {
        fun new(mode: AlarmsMode): AlarmsFragment = AlarmsFragment().also {
            it.mode = mode
        }

        const val MODE_ARG_KEY = "mode"
    }
}
