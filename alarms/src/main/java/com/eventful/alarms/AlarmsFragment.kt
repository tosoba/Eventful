package com.eventful.alarms

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.airbnb.epoxy.TypedEpoxyController
import com.eventful.alarms.databinding.FragmentAlarmsBinding
import com.eventful.alarms.dialog.AddEditAlarmDialog
import com.eventful.alarms.dialog.AddEditAlarmDialogMode
import com.eventful.alarms.dialog.AddEditAlarmDialogStatus
import com.eventful.alarms.dialog.showAddEditAlarmDialog
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
abstract class AlarmsFragment<M : AlarmsMode, VM : AlarmsViewModel> :
    DaggerViewModelFragment<VM>(R.layout.fragment_alarms),
    HasArgs {

    protected var mode: M by FragmentArgument(AlarmsArgs.MODE.name)
    override val args: Bundle get() = bundleOf(AlarmsArgs.MODE.name to mode)

    protected val binding: FragmentAlarmsBinding by viewBinding(FragmentAlarmsBinding::bind)

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
            loadMore = { lifecycleScope.launch { viewModel.intent(AlarmsIntent.LoadMoreAlarms) } }
        ) { selectable ->
            selectable.listItem(
                clicked = View.OnClickListener {
                    Toast.makeText(
                        context,
                        "Time left: ${selectable.item.formattedTimeLeft}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                longClicked = View.OnLongClickListener {
                    lifecycleScope.launch {
                        viewModel.intent(AlarmsIntent.AlarmLongClicked(selectable.item))
                    }
                    true
                },
                optionsButtonClicked = {
                    PopupMenu(it.context, it).apply {
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.action_edit_alarm -> {
                                    actionModeController.finish(false)
                                    lifecycleScope.launch {
                                        viewModel.intent(
                                            AlarmsIntent.UpdateDialogStatus(
                                                AddEditAlarmDialogStatus.WithMode.Shown(
                                                    AddEditAlarmDialogMode.Edit(alarm = selectable.item)
                                                )
                                            )
                                        )
                                    }
                                    true
                                }
                                R.id.action_show_event -> {
                                    actionModeController.finish(false)
                                    eventNavigationController?.showEvent(selectable.item.event)
                                    true
                                }
                                R.id.action_delete_alarm -> {
                                    lifecycleScope.launch {
                                        viewModel.intent(
                                            AlarmsIntent.DeleteAlarm(selectable.item.id)
                                        )
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                        gravity = Gravity.RIGHT
                        inflate(R.menu.alarm_item_options_menu)
                        show()
                    }
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

    private var addEditAlarmDialog: AddEditAlarmDialog? = null

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.viewUpdates
            .onEachLogging("VIEW_UPDATE", LogType.FRAGMENT) { update ->
                when (update) {
                    is AlarmsViewUpdate.Alarms -> epoxyController.setData(update.alarms)
                    is AlarmsViewUpdate.ShowDialog -> {
                        addEditAlarmDialog = showAddEditAlarmDialog(
                            mode = update.mode,
                            initialState = update.previousState
                        ) { timestamp ->
                            lifecycleScope.launch {
                                viewModel.intent(
                                    when (update.mode) {
                                        is AddEditAlarmDialogMode.Add -> AlarmsIntent.AddAlarm(
                                            event = update.mode.event,
                                            timestamp = timestamp
                                        )
                                        is AddEditAlarmDialogMode.Edit -> AlarmsIntent.UpdateAlarm(
                                            id = update.mode.alarm.id,
                                            timestamp = timestamp
                                        )
                                    }
                                )
                            }
                        }.apply {
                            fun updateDialogStatusToHidden() {
                                addEditAlarmDialog = null
                                lifecycleScope.launch {
                                    viewModel.intent(
                                        AlarmsIntent.UpdateDialogStatus(
                                            AddEditAlarmDialogStatus.Hidden
                                        )
                                    )
                                }
                            }
                            setOnCancelListener { updateDialogStatusToHidden() }
                            setOnDismissListener { updateDialogStatusToHidden() }
                        }
                    }
                }
            }
            .launchIn(lifecycleScope)

        with(binding.alarmsBottomNavView) {
            selectedItemId = R.id.bottom_nav_alarms
            setOnNavigationItemSelectedListener(eventNavigationItemSelectedListener)
        }

        binding.alarmsRecyclerView.setController(epoxyController)
    }

    override fun onDestroyView() {
        addEditAlarmDialog?.let {
            viewModel.viewModelScope.launch {
                viewModel.intent(
                    AlarmsIntent.UpdateDialogStatus(
                        status = AddEditAlarmDialogStatus.WithMode.ShownWithState(it.mode, it.state)
                    )
                )
            }
        }
        super.onDestroyView()
    }

    private var resumedOnlyViewUpdatesJob: Job? = null

    override fun onResume() {
        super.onResume()

        setupToolbar(binding.alarmsToolbar)
        showBackNavArrow()
        activity?.statusBarColor = context?.themeColor(R.attr.colorPrimaryDark)

        binding.alarmsBottomNavView.selectedItemId = R.id.bottom_nav_alarms

        viewModel.resumedOnlyViewUpdates
            .onEach { update ->
                when (update) {
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
        resumedOnlyViewUpdatesJob?.cancel()
        super.onPause()
    }
}
