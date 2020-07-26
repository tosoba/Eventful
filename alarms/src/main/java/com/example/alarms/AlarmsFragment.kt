package com.example.alarms

import android.os.Bundle
import androidx.core.os.bundleOf
import com.example.coreandroid.base.DaggerViewModelFragment
import com.example.coreandroid.base.HasArgs
import com.example.coreandroid.util.delegate.FragmentArgument
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class AlarmsFragment : DaggerViewModelFragment<AlarmsViewModel>(R.layout.fragment_alarms), HasArgs {

    private var mode: AlarmsMode by FragmentArgument()
    override val args: Bundle get() = bundleOf(MODE_ARG_KEY to mode)

    companion object {
        fun new(mode: AlarmsMode): AlarmsFragment = AlarmsFragment().also {
            it.mode = mode
        }

        const val MODE_ARG_KEY = "mode"
    }
}
