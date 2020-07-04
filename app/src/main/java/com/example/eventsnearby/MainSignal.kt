package com.example.eventsnearby

import com.example.coreandroid.provider.PopBackStackSignal

sealed class MainSignal {
    object PopMainBackStackSignal : MainSignal(), PopBackStackSignal
}
