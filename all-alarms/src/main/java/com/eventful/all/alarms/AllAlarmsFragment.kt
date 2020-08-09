package com.eventful.all.alarms

import android.os.Bundle
import android.view.View
import com.eventful.alarms.AlarmsFragment
import com.eventful.alarms.AlarmsMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class AllAlarmsFragment : AlarmsFragment<AlarmsMode.All, AllAlarmsViewModel>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.alarmsFab.visibility = View.GONE
        binding.alarmsBottomNavView.visibility = View.GONE
    }

    companion object {
        val new: AllAlarmsFragment get() = AllAlarmsFragment().also { it.mode = AlarmsMode.All }
    }
}
