package com.example.coreandroid.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TitledFragmentsPagerAdapter(
    private val classLoader: ClassLoader,
    private val fragmentManager: FragmentManager,
    private val fragmentData: Array<TitledFragmentData>
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        val data = fragmentData[position]
        val fragment = fragmentManager.fragmentFactory.instantiate(classLoader, data.clazz.name)
        if (data.args != null) fragment.arguments = data.args
        return fragment
    }

    override fun getCount(): Int = fragmentData.size
    override fun getPageTitle(position: Int): CharSequence? = fragmentData[position].title
}

class TitledFragmentData(
    val clazz: Class<out Fragment>,
    val title: String,
    val args: Bundle? = null
)