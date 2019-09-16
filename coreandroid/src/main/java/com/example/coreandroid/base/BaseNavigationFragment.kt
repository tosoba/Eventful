package com.example.coreandroid.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coreandroid.util.ext.hideBackNavArrow


abstract class BaseNavigationFragment : Fragment() {

    protected abstract val initialFragment: Fragment
    protected abstract val navigationFragmentLayoutId: Int
    protected abstract val backStackNavigationContainerId: Int

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(navigationFragmentLayoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null)
            showFragment(initialFragment, false)
    }

    fun onBackPressed(): Boolean {
        val backStackEntryCount = childFragmentManager.backStackEntryCount
        if (backStackEntryCount >= 1) {
            childFragmentManager.popBackStack()
            if (backStackEntryCount == 1) hideBackNavArrow()
            return true
        }
        return false
    }

    fun showFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        with(childFragmentManager.beginTransaction()) {
            setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            replace(backStackNavigationContainerId, fragment)
            if (addToBackStack) addToBackStack(null)
            commit()
        }
    }
}
