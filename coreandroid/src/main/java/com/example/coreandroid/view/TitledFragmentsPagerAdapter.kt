package com.example.coreandroid.view

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TitledFragmentsPagerAdapter(
    fragmentManager: FragmentManager,
    private val titledFragments: Array<Pair<String, Fragment>>
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = titledFragments[position].second

    override fun getCount(): Int = titledFragments.size

    override fun getPageTitle(position: Int): CharSequence? = titledFragments[position].first
}