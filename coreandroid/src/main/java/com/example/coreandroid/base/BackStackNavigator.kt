package com.example.coreandroid.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.example.coreandroid.util.ext.hideBackNavArrow
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

interface BackStackNavigator {
    fun <T : Fragment> addFragment(
        fragmentClass: Class<T>,
        addToBackStack: Boolean = true,
        args: Bundle? = null
    )
}
