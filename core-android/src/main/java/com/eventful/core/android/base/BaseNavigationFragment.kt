package com.eventful.core.android.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.eventful.core.android.util.ext.castTo
import com.eventful.core.android.util.ext.hideBackNavArrow
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

@ExperimentalCoroutinesApi
@FlowPreview
abstract class BaseNavigationFragment : DaggerFragment() {

    protected abstract val initialFragment: Fragment
    protected abstract val navigationFragmentLayoutId: Int
    protected abstract val backStackNavigationContainerId: Int

    val currentTopFragment: Fragment?
        get() = childFragmentManager.findFragmentById(backStackNavigationContainerId)

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(navigationFragmentLayoutId, container, false)

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) showFragment(initialFragment, false)
    }

    override fun onDestroy() {
        backStackSignalsChannel.close()
        super.onDestroy()
    }

    private val backStackSignalsChannel: BroadcastChannel<Boolean> =
        BroadcastChannel(capacity = Channel.CONFLATED)
    val backStackSignals: Flow<Boolean>
        get() = backStackSignalsChannel.asFlow()

    fun handleBackPressedOrPopBackStack() {
        currentTopFragment?.castTo<BackPressedHandler>()?.onBackPressed() ?: popBackStack()
    }

    fun popBackStack() {
        val backStackEntryCount = childFragmentManager.backStackEntryCount
        if (backStackEntryCount >= 1) {
            childFragmentManager.popBackStack()
            if (backStackEntryCount == 1) hideBackNavArrow()
            backStackSignalsChannel.trySendBlocking(true)
        } else {
            backStackSignalsChannel.trySendBlocking(false)
        }
    }

    fun showFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        with(childFragmentManager.beginTransaction()) {
            setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out)
            add(backStackNavigationContainerId, fragment)
            if (addToBackStack) addToBackStack(null)
            commit()
        }
    }
}
