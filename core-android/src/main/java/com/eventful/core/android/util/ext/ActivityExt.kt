package com.eventful.core.android.util.ext

import android.app.Activity
import android.os.Build
import android.view.WindowManager

var Activity.statusBarColor: Int?
    set(value) {
        if (value != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = value
        }
    }
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window?.statusBarColor else null
