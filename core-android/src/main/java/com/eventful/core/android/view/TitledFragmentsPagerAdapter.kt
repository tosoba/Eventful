package com.eventful.core.android.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TitledFragmentsPagerAdapter(
    fragmentManager: FragmentManager,
    val titledFragments: Array<out Pair<String, Fragment>>
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment = titledFragments[position].second
    override fun getCount(): Int = titledFragments.size
    override fun getPageTitle(position: Int): CharSequence? = titledFragments[position].first

    inline fun <reified F> containsFragmentOfType(): Boolean =
        titledFragments.any { it.second is F }
}

inline fun Fragment.titledFragmentsPagerAdapter(
    crossinline titledFragments: () -> Array<out Pair<String, Fragment>>
): Lazy<TitledFragmentsPagerAdapter> =
    lazy(LazyThreadSafetyMode.NONE) {
        TitledFragmentsPagerAdapter(childFragmentManager, titledFragments())
    }
