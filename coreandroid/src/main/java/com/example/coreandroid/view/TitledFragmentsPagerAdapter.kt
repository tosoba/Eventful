package com.example.coreandroid.view

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TitledFragmentsPagerAdapter(
    fragmentManager: FragmentManager,
    private val titledFragments: Array<Pair<String, Fragment>>
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var previousFragment: Fragment? = null
        private set

    var currentFragment: Fragment? = null
        private set

    override fun getItem(position: Int): Fragment = titledFragments[position].second

    override fun getCount(): Int = titledFragments.size

    override fun getPageTitle(position: Int): CharSequence? = titledFragments[position].first

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (currentFragment != `object` && `object` is Fragment) {
            previousFragment = currentFragment
            currentFragment = `object`
        }
        super.setPrimaryItem(container, position, `object`)
    }
}