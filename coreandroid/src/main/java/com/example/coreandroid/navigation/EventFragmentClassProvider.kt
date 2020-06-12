package com.example.coreandroid.navigation

import androidx.fragment.app.Fragment
import com.example.coreandroid.model.Event

interface EventFragmentClassProvider {
    val eventFragmentClass: Class<out Fragment>
}
