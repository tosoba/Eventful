package com.eventful

import com.eventful.core.android.provider.PopBackStackSignal

sealed class MainSignal {
    object PopMainBackStackSignal : MainSignal(), PopBackStackSignal
}
