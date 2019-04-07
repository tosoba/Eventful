package com.example.eventsnearby

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TitledFragmentsPagerAdapter(
    fragmentManager: FragmentManager,
    private val titledFragments: Array<Pair<String, Fragment>>
) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int): Fragment = titledFragments[position].second
    override fun getCount(): Int = titledFragments.size
    override fun getPageTitle(position: Int): CharSequence? = titledFragments[position].first
}