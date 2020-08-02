package com.eventful.alarms

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.TypedEpoxyController
import com.eventful.alarms.databinding.FragmentAlarmsBinding
import com.eventful.core.android.base.DaggerViewModelFragment
import com.eventful.core.android.base.HasArgs
import com.eventful.core.android.controller.ItemsSelectionActionModeController
import com.eventful.core.android.controller.eventNavigationItemSelectedListener
import com.eventful.core.android.controller.itemsSelectionActionModeController
import com.eventful.core.android.model.alarm.Alarm
import com.eventful.core.android.navigation.IMainChildFragmentNavDestinations
import com.eventful.core.android.util.delegate.FragmentArgument
import com.eventful.core.android.util.delegate.viewBinding
import com.eventful.core.android.util.ext.*
import com.eventful.core.android.view.epoxy.EpoxyThreads
import com.eventful.core.android.view.epoxy.infiniteItemListController
import com.eventful.core.model.Selectable
import com.eventful.core.util.HoldsList
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
    internal lateinit var fragmentFactory: IMainChildFragmentNavDestinations

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
        when (val modeArg = mode) {
            is AlarmsMode.All -> {
                binding.alarmsFab.visibility = View.GONE
                binding.alarmsBottomNavView.visibility = View.GONE
            }
            is AlarmsMode.SingleEvent -> binding.alarmsFab.setOnClickListener {
                showAddEditAlarmDialog(AlarmMode.Add(event = modeArg.event)) { timestamp ->
                    //viewModel.intent(AlarmsIntent.AddAlarm())
                }
            }
        }

        with(binding.alarmsBottomNavView) {
            selectedItemId = R.id.bottom_nav_alarms
            setOnNavigationItemSelectedListener(eventNavigationItemSelectedListener)
        }

        binding.alarmsRecyclerView.setController(epoxyController)
    }

    private var viewUpdatesJob: Job? = null

    override fun onResume() {
        super.onResume()

        setupToolbar(binding.alarmsToolbar)
        showBackNavArrow()
        activity?.statusBarColor = context?.themeColor(R.attr.colorPrimaryDark)

        binding.alarmsBottomNavView.selectedItemId = R.id.bottom_nav_alarms

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
