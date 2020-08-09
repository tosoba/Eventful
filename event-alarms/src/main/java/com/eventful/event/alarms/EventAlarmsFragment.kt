package com.eventful.event.alarms

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.eventful.alarms.AlarmsFragment
import com.eventful.alarms.AlarmsIntent
import com.eventful.alarms.AlarmsMode
import com.eventful.alarms.dialog.AddEditAlarmDialogMode
import com.eventful.alarms.dialog.AddEditAlarmDialogStatus
import com.eventful.core.android.model.event.Event
import com.eventful.core.android.util.delegate.FragmentArgument
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class EventAlarmsFragment : AlarmsFragment<AlarmsMode.SingleEvent, EventAlarmsViewModel>() {

    private var bottomNavItemsToRemove: IntArray by FragmentArgument()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomNavItemsToRemove.forEach(binding.alarmsBottomNavView.menu::removeItem)
        binding.alarmsFab.setOnClickListener {
            lifecycleScope.launch {
                viewModel.intent(
                    AlarmsIntent.UpdateDialogStatus(
                        AddEditAlarmDialogStatus.WithMode.Shown(
                            AddEditAlarmDialogMode.Add(event = mode.event)
                        )
                    )
                )
            }
        }
    }

    companion object {
        fun new(
            event: Event,
            bottomNavItemsToRemove: IntArray
        ): EventAlarmsFragment = EventAlarmsFragment().also {
            it.mode = AlarmsMode.SingleEvent(event)
            it.bottomNavItemsToRemove = bottomNavItemsToRemove
        }
    }
}
